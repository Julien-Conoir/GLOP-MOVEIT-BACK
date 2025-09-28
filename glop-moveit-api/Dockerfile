# Dockerfile multi-stage pour optimiser la taille de l'image

# Stage 1: Build de l'application avec Maven
FROM maven:3.9.4-eclipse-temurin-21 as builder

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers pom.xml d'abord (pour optimiser le cache Docker)
COPY pom.xml ./pom.xml
COPY glop-moveit-api/pom.xml ./glop-moveit-api/pom.xml

# Installer le POM parent dans le repository local Maven d'abord
RUN mvn install -N -f pom.xml

# Télécharger les dépendances (cette couche sera mise en cache si les pom.xml n'ont pas changé)
RUN mvn dependency:go-offline -B -f glop-moveit-api/pom.xml

# Copier le code source
COPY glop-moveit-api/src ./glop-moveit-api/src

# Compiler et packager l'application
RUN mvn clean package -DskipTests -f glop-moveit-api/pom.xml

# Stage 2: Image finale avec JRE seulement
FROM eclipse-temurin:21-jre-alpine

# Informations sur l'image
LABEL maintainer="GLOP MoveIT Team"
LABEL description="API Spring Boot pour GLOP MoveIT"
LABEL version="1.0.0"

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR depuis le stage builder
COPY --from=builder /app/glop-moveit-api/target/*.jar app.jar

# Changer le propriétaire des fichiers
RUN chown -R appuser:appgroup /app

# Utiliser l'utilisateur non-root
USER appuser

# Exposer le port
EXPOSE 8080

# Définir les variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Point d'entrée de l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Vérification de santé
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/users/health || exit 1