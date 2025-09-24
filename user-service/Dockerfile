FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Thêm biến môi trường để tắt metrics liên quan Cgroup
ENV JAVA_TOOL_OPTIONS="-Dspring.metrics.use-cgroup=false"

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
