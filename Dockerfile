FROM openjdk:17-slim

WORKDIR /app

COPY target/pagamento-api-0.0.1-SNAPSHOT.jar /app/pagamento-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/pagamento-api.jar"]