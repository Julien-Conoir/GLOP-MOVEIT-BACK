# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml ./
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier le JAR depuis le build stage
COPY --from=build /app/target/*.jar app.jar

# Exposer le port de l'application
EXPOSE 8082

# Variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=prod

# Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
