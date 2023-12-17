FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/springboot-minikube.jar
COPY ${JAR_FILE} springboot-minikube.jar
ENTRYPOINT ["java","-jar","/springboot-minikube.jar"]
EXPOSE 8080
