# Builder stage
FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

# Final stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Dspring.metrics.use-cgroup=false"
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]