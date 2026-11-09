# Liqbez Demo: pet clinic

Стенд для примеров микросервисов, API Gateway и Service Discovery. Все демо развиваются в `main`.

| Компонент | Адрес с хоста | Назначение |
|---|---|---|
| `services/eureka-service` | `http://localhost:8761` | Активный реестр Eureka |
| `services/api-gateway` | `http://localhost:18080` | Внешний вход |
| `services/patient-service` | Через gateway | Демо-данные пациента |
| `services/appointment-service` | Через gateway | Запись на приём; вызывает `patient-service` по имени; содержит демо XA, Saga и синхронизации транзакции с файловым хранилищем |
| `services/payment-service` | Через gateway | Локальная запись платежа для Saga |
| Consul | Не запускается в Compose | Сохранённая альтернативная конфигурация |

Gateway использует `lb://patient-service` и `lb://appointment-service`. Сервисы регистрируются в Eureka через Spring Cloud Netflix. Настройки Consul сохранены в комментариях.

## Вертикальные срезы и слои

Код сгруппирован сначала по сценарию, затем по слою. В `appointment-service` пять срезов:

```text
ru.liqbez/
├── AppointmentServiceApplication.java
├── appointment/   controller, service, repository, model, client
├── importsync/    controller, service, repository, model, storage
├── reservation/   controller, service, repository, model
├── saga/          controller, service, repository, model, client
└── xa/            controller, service, repository, model
```

В `patient-service` есть срез `patient`, в `payment-service` — срез `payment`; внутри каждого те же `controller`, `service`, `repository`, `model`. Класс запуска Spring Boot лежит в `ru.liqbez` каждого микросервиса и сканирует все его срезы.

Контроллер принимает HTTP-запрос, сервис выполняет сценарий, репозиторий хранит или читает данные, `client` вызывает другой микросервис. `SagaService` обращается к `ReservationService` соседнего среза и к `PaymentClient`; результаты Saga хранятся в памяти через `SagaRepository`. Координатор `XaTwoPhaseCommitService` управляет фазами XA, а `XaAuditRepository` работает с двумя PostgreSQL. `PatientRepository` и `AppointmentRepository` пока содержат демо-данные в памяти.

## Запуск в Docker Compose

Нужны Docker Compose и JDK 25 для сборки JAR. Из корня проекта:

```bash
./gradlew bootJar
docker compose up --build -d
```

Открой [Eureka UI](http://localhost:8761) и дождись регистрации трёх приложений. Проверь запросы:

```bash
curl http://localhost:18080/patients/1
curl http://localhost:18080/appointments/1
```

Ожидаются пациент Барсик и запись с вложенным пациентом. Порт `18080` выбран для Compose, чтобы не конфликтовать с локально запущенным gateway на `8080`. Приложения внутри Compose работают на своих обычных портах.

Вторая реплика сервиса пациентов:

```bash
docker compose up -d --scale patient-service=2
```

В Eureka появятся два экземпляра `PATIENT-SERVICE`. Остановить стенд: `docker compose down`.

## Демо 2PC / XA

`XaDemoController` вызывает `XaTwoPhaseCommitService` внутри `appointment-service`. Сервис подключается к двум отдельным PostgreSQL через `PGXADataSource`. Для каждого запроса он создаёт два XA branch с общим transaction ID, записывает строки, вызывает `prepare` у обеих БД и затем `commit`. В Compose у PostgreSQL включён `max_prepared_transactions`.

```bash
XA_ID=$(uuidgen)
curl -X POST "http://localhost:18080/transactions/xa/$XA_ID"
curl "http://localhost:18080/transactions/xa/$XA_ID"
```

В ответе обе БД должны быть `true`. Для проверки отката используй новый ID:

```bash
XA_FAIL_ID=$(uuidgen)
curl -i -X POST "http://localhost:18080/transactions/xa/$XA_FAIL_ID?failBeforePrepare=true"
curl "http://localhost:18080/transactions/xa/$XA_FAIL_ID"
```

Первый запрос намеренно получает ошибку; второй показывает `false` для обеих БД. Это **настоящие XA prepare/commit/rollback**, но координатор написан как учебный пример: у него нет долговечного журнала решения и автоматического восстановления после падения между `prepare` и завершением `commit`. Для такой точки отказа этот код не подходит для продакшена.

## Демо Saga

`SagaDemoController` вызывает `SagaService` внутри `appointment-service`. Тот локально сохраняет бронь через `ReservationService`, затем вызывает `payment-service` по сети. При отказе платежа координатор выполняет отдельную операцию отмены брони. Это компенсация уже завершённого шага, а не общий rollback.

```bash
SAGA_ID=$(uuidgen)
curl -X POST "http://localhost:18080/transactions/saga/$SAGA_ID"
curl "http://localhost:18080/appointments/reservations/$SAGA_ID"
curl "http://localhost:18080/payments/$SAGA_ID"
```

Для отказа платежа используй новый ID:

```bash
SAGA_FAIL_ID=$(uuidgen)
curl -X POST "http://localhost:18080/transactions/saga/$SAGA_FAIL_ID?failPayment=true"
curl "http://localhost:18080/appointments/reservations/$SAGA_FAIL_ID"
```

Ожидаются `COMPENSATED` и бронь со статусом `CANCELLED`. Бронь и платёж хранятся в отдельных файловых H2 внутри Docker volumes; состояние координатора Saga пока хранится в памяти. В демо нет восстановления незавершённых Saga и разрешения неоднозначного результата сетевого вызова после оплаты.

## Демо транзакционной синхронизации: БД + файл

Срез `importsync` повторяет разделение ответственности из [VehicleImportService](https://github.com/r4m63/is-itmo/blob/main/src/main/java/ru/itmo/isitmolab/service/VehicleImportService.java) и [VehicleImportTxService](https://github.com/r4m63/is-itmo/blob/main/src/main/java/ru/itmo/isitmolab/service/VehicleImportTxService.java), но использует Spring. `PetVisitImportService` создаёт запись `STARTED` и проверяет список питомцев. `PetVisitImportTxService` в локальной транзакции сохраняет строки в H2, через `TransactionSynchronization.beforeCommit` загружает текстовый файл в хранилище, а в `afterCompletion` записывает итог операции. При откате после загрузки он удаляет файл. Записи о статусе выполняются отдельными транзакциями `REQUIRES_NEW`.

По умолчанию хранилище — каталог `/data/import-objects` внутри Docker volume `appointment-data`. Для использования доступного извне MinIO можно запустить Compose с `IMPORT_STORAGE=minio` и задать `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`. В `appointment-service` есть отдельный `MinioObjectStorage`; сервер MinIO в этот Compose не включён.

```bash
IMPORT_ID=$(uuidgen)
curl -X POST "http://localhost:18080/transactions/synchronization/imports/$IMPORT_ID" \
  -H 'Content-Type: application/json' \
  -d '{"fileName":"visits.txt","petNames":["Барсик","Мурка"]}'
curl "http://localhost:18080/transactions/synchronization/imports/$IMPORT_ID"
```

Ожидаются `COMPLETED`, два имени в `petNames` и `objectPresent: true`. Для проверки отката после успешной загрузки:

```bash
IMPORT_FAIL_ID=$(uuidgen)
curl -i -X POST "http://localhost:18080/transactions/synchronization/imports/$IMPORT_FAIL_ID?failAfterUpload=true" \
  -H 'Content-Type: application/json' \
  -d '{"fileName":"visits.txt","petNames":["Шарик"]}'
curl "http://localhost:18080/transactions/synchronization/imports/$IMPORT_FAIL_ID"
```

Первый запрос намеренно возвращает ошибку. Второй показывает `FAILED`, пустой список и `objectPresent: false`. Это **не XA/2PC между H2 и файловым хранилищем**: файл загружается до commit БД, а при откате удаляется отдельным действием. Падение процесса между этими шагами может оставить лишний файл; при неопределённом исходе транзакции пример оставляет статус `UNKNOWN` для разбора, а не удаляет файл вслепую.

## Альтернатива: Consul

Конфигурация Consul сохранена в комментариях `compose.yaml`, Gradle-файлов и `application.yaml`. Для переключения нужно сделать её активной в трёх приложениях и Compose, а конфигурацию Eureka закомментировать. Оба реестра не запускаются одновременно в этом демо.
