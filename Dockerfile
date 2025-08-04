# Build stage (Maven + JDK 21)
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src src

# Build the JAR (skipping tests for faster builds)
RUN mvn clean package -DskipTests

# Runtime stage (Alpine + JRE 21)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# (Optional) Install extra Alpine packages if needed (e.g., for fonts)
# RUN apk add --no-cache fontconfig ttf-dejavu

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]