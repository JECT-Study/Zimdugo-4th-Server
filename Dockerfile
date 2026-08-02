FROM eclipse-temurin:25-jre@sha256:681c543d6f36c50f45e9b5226930a46203dcfa351d3670e9d0bdf0dabae53539

RUN groupadd --system zimdugo \
    && useradd --system --gid zimdugo --home-dir /app --no-create-home --shell /usr/sbin/nologin zimdugo

WORKDIR /app

COPY --chown=zimdugo:zimdugo build/libs/*.jar app.jar

EXPOSE 8080

USER zimdugo

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]
