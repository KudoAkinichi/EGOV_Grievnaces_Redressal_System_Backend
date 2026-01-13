FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=grievance-service/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
