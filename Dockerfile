FROM openjdk:21
WORKDIR /scholarship
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} ./scholarship-on-backend.jar
ENTRYPOINT ["java","-jar","scholarship-on-backend.jar"]