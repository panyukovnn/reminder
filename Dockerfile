FROM openjdk:21-slim

COPY ./build/libs/reminder.jar /reminder.jar

EXPOSE 8008
ENV TZ=Europe/Moscow

ENTRYPOINT ["java", "-jar", "/reminder.jar"]