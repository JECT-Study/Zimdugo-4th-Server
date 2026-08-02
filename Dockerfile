FROM eclipse-temurin:25-jre@sha256:681c543d6f36c50f45e9b5226930a46203dcfa351d3670e9d0bdf0dabae53539

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]
