# Liqbez Demo: pet clinic

Стенд для примеров микросервисов, API Gateway и Service Discovery. Все демо развиваются в `main`.

| Компонент | Адрес с хоста | Назначение |
|---|---|---|
| `services/eureka-service` | `http://localhost:8761` | Активный реестр Eureka |
| `services/api-gateway` | `http://localhost:18080` | Внешний вход |
| `services/patient-service` | Через gateway | Демо-данные пациента |
| `services/appointment-service` | Через gateway | Запись на приём; вызывает `patient-service` по имени |
| Consul | Не запускается в Compose | Сохранённая альтернативная конфигурация |

Gateway использует `lb://patient-service` и `lb://appointment-service`. Сервисы регистрируются в Eureka через Spring Cloud Netflix. Настройки Consul сохранены в комментариях.

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

## Альтернатива: Consul

Конфигурация Consul сохранена в комментариях `compose.yaml`, Gradle-файлов и `application.yaml`. Для переключения нужно сделать её активной в трёх приложениях и Compose, а конфигурацию Eureka закомментировать. Оба реестра не запускаются одновременно в этом демо.
