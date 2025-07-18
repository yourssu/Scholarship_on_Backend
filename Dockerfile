FROM openjdk:21
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} ./scholarship-on-backend.jar
RUN mkdir -p /scholarship
ENTRYPOINT ["java","-jar","scholarship-on-backend.jar"]