# Liqbez Demo: pet clinic

Стенд для примеров микросервисов, API Gateway и Service Discovery. Все демо развиваются в `main`.

| Компонент | Адрес с хоста | Назначение |
|---|---|---|
| Consul | `http://localhost:8500/ui` | Активный реестр сервисов |
| `services/api-gateway` | `http://localhost:18080` | Внешний вход |
| `services/patient-service` | Через gateway | Демо-данные пациента |
| `services/appointment-service` | Через gateway | Запись на приём; вызывает `patient-service` по имени |
| `services/eureka-service` | Не запускается в Compose | Сохранённый альтернативный реестр Eureka |

Gateway использует `lb://patient-service` и `lb://appointment-service`. Сервисы регистрируются в Consul через Spring Cloud Consul. Consul проверяет их `/actuator/health`.

## Запуск в Docker Compose

Нужны Docker Compose и JDK 25 для сборки JAR. Из корня проекта:

```bash
./gradlew :services:patient-service:bootJar :services:appointment-service:bootJar :services:api-gateway:bootJar
docker compose up --build -d
```

Открой [Consul UI](http://localhost:8500/ui) и дождись трёх здоровых сервисов. Проверь запросы:

```bash
curl http://localhost:18080/patients/1
curl http://localhost:18080/appointments/1
```

Ожидаются пациент Барсик и запись с вложенным пациентом. Порт `18080` выбран для Compose, чтобы не конфликтовать с локально запущенным gateway на `8080`. Приложения внутри Compose работают на своих обычных портах.

Вторая реплика сервиса пациентов:

```bash
docker compose up -d --scale patient-service=2
```

В Consul появятся два экземпляра `patient-service`. Остановить стенд: `docker compose down`.

## Альтернатива: Eureka

Модуль `services/eureka-service` сохранён. Для возврата к нему нужно заменить активную зависимость `spring-cloud-starter-consul-discovery` на закомментированную `spring-cloud-starter-netflix-eureka-client` в трёх приложениях, вернуть закомментированные блоки `eureka` в их `application.yaml` и запустить `./gradlew :services:eureka-service:bootRun`. Consul и Eureka не запускаются одновременно в этом демо.
