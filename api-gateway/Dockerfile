FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=api-gateway/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 7070
ENTRYPOINT ["java","-jar","/app/app.jar"]
