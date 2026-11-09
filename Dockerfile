FROM eclipse-temurin:25-jre

ARG SERVICE
COPY services/${SERVICE}/build/libs/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
