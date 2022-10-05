FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/springboot.jar
COPY ${JAR_FILE} springboot.jar
ENTRYPOINT ["java","-jar","/springboot.jar"]
EXPOSE 8080
