FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew bootJar -x test
RUN cp build/libs/app.jar /workspace/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/app.jar /app/app.jar
EXPOSE 8084 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
