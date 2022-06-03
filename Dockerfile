FROM openjdk:18.0.1
COPY target/exchange-rate-service.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]