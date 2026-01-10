FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew bootJar -x test
RUN cp build/libs/app.jar /workspace/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends sops age ca-certificates \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/app.jar /app/app.jar
COPY secrets /app/secrets
EXPOSE 8084 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
