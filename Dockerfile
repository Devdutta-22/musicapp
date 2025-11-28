# ---- BUILD STAGE ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the entire repository into the image
COPY . .

# Ensure the Maven wrapper is executable (some filesystems lose exec bit)
RUN chmod +x mvnw || true

# Build the project (skip tests to speed up the build)
RUN ./mvnw -B -DskipTests package

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the application jar produced by the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (app reads $PORT at runtime)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
