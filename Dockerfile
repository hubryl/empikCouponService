FROM eclipse-temurin:21
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
