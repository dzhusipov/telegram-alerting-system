FROM openjdk:11
COPY . /usr/src/telegram-alerts
WORKDIR /usr/src/telegram-alerts
RUN ./gradlew build
EXPOSE 8080
CMD ["java", "-jar", "build/libs/telegram-alerting-system-0.0.1-SNAPSHOT.jar"]