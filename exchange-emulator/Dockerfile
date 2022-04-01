FROM openjdk:8-jre-slim

EXPOSE 8080

RUN mkdir /app

COPY exchange-emulator-1.0-SNAPSHOT-all.jar /app/exchange-emulator.jar

ENTRYPOINT ["java", "-jar", "/app/exchange-emulator.jar"]