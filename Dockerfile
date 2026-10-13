# One image that runs the whole site: the Spring Boot backend also serves the built React website.
# One address, one program, so no CORS and no separate frontend hosting. Build from the repository root.

# ---- 1. Build the website (React + Vite) ----
FROM node:22-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- 2. Build the backend, with the finished website placed inside it (served from /) ----
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY backend/pom.xml ./
# Download dependencies first, so they are cached until pom.xml changes
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/dist ./src/main/resources/static
# Tests need a database, so they run in development, not while building the image
RUN mvn -q -B -DskipTests package

# ---- 3. The small image that actually runs ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar

# Never run as root inside the container
RUN useradd --system --no-create-home appuser
USER appuser

# "prod" is the active profile, so the development settings (profile "local") are never used here.
# Everything secret (database, ADMIN_PASSWORD, PAYMENT_PROVIDER, ...) comes in as environment variables.
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
