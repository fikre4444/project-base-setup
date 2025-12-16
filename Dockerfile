FROM openjdk:21-rc-jdk-slim

WORKDIR /app

COPY target/sample-service-1.1.10.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]