FROM openjdk:21
WORKDIR /scholarship
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} scholarship-on-backend.jar
VOLUME /scholarship
ENTRYPOINT ["java","-jar","/scholarship/scholarship-on-backend.jar"]