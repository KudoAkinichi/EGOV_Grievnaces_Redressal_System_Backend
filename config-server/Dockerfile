FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=config-server/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
