FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=feedback-service/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
