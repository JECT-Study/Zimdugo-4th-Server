FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]
