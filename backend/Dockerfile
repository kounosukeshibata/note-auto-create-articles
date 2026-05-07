# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Dependency cache layer (gradle.properties excluded to avoid local java.home path)
COPY gradle gradle
COPY gradlew settings.gradle.kts build.gradle.kts ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q 2>/dev/null || true

# Build jar (skip tests; tests run in CI)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime (JRE only)
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache jq
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
COPY entrypoint.sh ./
RUN chmod +x entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
