# Builder stage
FROM eclipse-temurin:17-jdk as build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY src src
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Final stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Copy wait-for-it.sh vào image và cấp quyền
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

EXPOSE 8888

ENTRYPOINT ["/app/wait-for-it.sh", "eureka:8761", "--timeout=60", "--strict", "--", "java", "-jar", "app.jar"]
