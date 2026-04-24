FROM gradle:8.10.2-jdk21-alpine AS build
WORKDIR /workspace

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
