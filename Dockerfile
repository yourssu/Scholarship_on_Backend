FROM openjdk:21-jre-slim
WORKDIR /scholarship
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} ./scholarship-on-backend.jar
VOLUME /scholarship
ENTRYPOINT ["java","-jar","scholarship-on-backend.jar"]