FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/arm-support-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
