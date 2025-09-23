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

# Copy wait-for-it.sh và cấp quyền
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

EXPOSE 8081

# Chờ postgres rồi eureka
ENTRYPOINT ["/app/wait-for-it.sh", "postgres:5432", "--timeout=60", "--strict", "--", "/app/wait-for-it.sh", "eureka:8761", "--timeout=60", "--strict", "--", "java", "-jar", "app.jar"]
