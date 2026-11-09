# Liqbez Demo: pet clinic

Минимальный стенд для изучения микросервисов, API Gateway и Service Discovery. Все примеры развиваются в `main`.

## Состав

| Приложение | Порт | Роль |
|---|---:|---|
| `services/discovery-server` | 8761 | Eureka: реестр экземпляров |
| `services/api-gateway` | 8080 | Внешний вход, маршрутизация по именам сервисов |
| `services/patient-service` | 8081 | Демо-данные пациента |
| `services/appointment-service` | 8082 | Запись на приём, запрос к `patient-service` по имени |

Gateway использует маршруты `lb://patient-service` и `lb://appointment-service`. `appointment-service` обращается к `http://patient-service` через Spring Cloud LoadBalancer. Порты сервисов нужны лишь для локального запуска; адреса маршрутов не зависят от них.

## Запуск

Требуется JDK 25. В четырёх терминалах из корня проекта запустить по порядку:

```bash
./gradlew :services:discovery-server:bootRun
./gradlew :services:patient-service:bootRun
./gradlew :services:appointment-service:bootRun
./gradlew :services:api-gateway:bootRun
```

После регистрации сервисов в Eureka открыть `http://localhost:8761` и проверить:

```bash
curl http://localhost:8080/patients/1
curl http://localhost:8080/appointments/1
```

Ожидаются пациент Барсик и запись с вложенным пациентом. Данные пока находятся в памяти и заданы для `id=1`, чтобы видеть сетевой маршрут без БД. Следующие примеры добавляются как отдельные сценарии или модули в этот же репозиторий.
