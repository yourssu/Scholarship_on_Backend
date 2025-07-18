FROM openjdk:21
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /scholarship/scholarship-on-backend.jar
WORKDIR /scholarship
VOLUME /scholarship
ENTRYPOINT ["java","-jar","/scholarship/scholarship-on-backend.jar"]