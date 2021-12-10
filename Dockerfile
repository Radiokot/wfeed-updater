FROM openjdk:8-jdk-alpine
COPY build/libs/updater.jar /updater.jar
ENTRYPOINT java \
$(test -f /log4j.properties && echo "-Dlog4j.configuration=file:/log4j.properties") \
-jar /updater.jar