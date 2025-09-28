# GLOP MoveIT - Backend

## Description

Projet multi-module Maven avec une API Spring Boot pour l'application GLOP MoveIT, utilisant Java 21, PostgreSQL et Docker.

## Architecture

```
glop-moveit-parent/
├── docker-compose.yml
├── start-dev.sh / start-dev.bat
├── init-scripts/
│   └── 01-init.sql
├── pom.xml (parent)
└── glop-moveit-api/
    ├── Dockerfile
    ├── .dockerignore
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/glop/moveit/api/
        │   │   ├── GlopMoveItApiApplication.java
        │   │   ├── controller/UserController.java
        │   │   ├── service/UserService.java
        │   │   ├── repository/UserRepository.java
        │   │   └── model/User.java
        │   └── resources/
        │       ├── application.yml
        │       ├── application-docker.yml
        │       └── data.sql
        └── test/
            ├── java/com/glop/moveit/api/
            │   └── GlopMoveItApiApplicationTests.java
            └── resources/
                └── application-test.yml
```

## Technologies utilisées

- **Java 21** (LTS)
- **Spring Boot 3.1.5**
- **Spring Data JPA**
- **PostgreSQL 15** (base de données principale)
- **H2 Database** (tests uniquement)
- **Docker & Docker Compose**
- **Maven** (gestion des dépendances)
- **JUnit 5** (tests unitaires)

## 🚀 Démarrage rapide avec Docker

### Prérequis
- **Docker** et **Docker Compose** installés
- **Java 21** (optionnel, pour développement local)
- **Maven 3.6+** (optionnel, pour développement local)

### Lancement avec Docker Compose

#### Windows
```cmd
# Utiliser le script automatisé
start-dev.bat

# Ou manuellement
docker-compose up --build -d
```

#### Linux/Mac
```bash
# Utiliser le script automatisé
chmod +x start-dev.sh
./start-dev.sh

# Ou manuellement
docker-compose up --build -d
```

### Services disponibles

| Service | URL | Description |
|---------|-----|-------------|
| API | http://localhost:8080 | API REST Spring Boot |
| PostgreSQL | localhost:5432 | Base de données |
| pgAdmin | http://localhost:5050 | Interface d'administration DB |

### Accès PostgreSQL
- **Host:** localhost:5432
- **Database:** glop_moveit_db  
- **Username:** glop_user
- **Password:** glop_password

### Accès pgAdmin (optionnel)
```bash
# Démarrer avec pgAdmin
docker-compose --profile admin up -d
```
- **URL:** http://localhost:5050
- **Email:** admin@glop-moveit.com
- **Password:** admin123

## 📋 API Endpoints

### Health Check
```bash
GET /api/users/health
```

### Gestion des Utilisateurs
- `GET /api/users` - Liste tous les utilisateurs
- `GET /api/users/{id}` - Récupère un utilisateur par ID
- `POST /api/users` - Crée un utilisateur
- `PUT /api/users/{id}` - Met à jour un utilisateur  
- `DELETE /api/users/{id}` - Supprime un utilisateur

## 🔧 Développement local (sans Docker)

### Prérequis
- Java 21
- Maven 3.6+
- PostgreSQL 15+ installé localement

### Configuration PostgreSQL locale
1. Créer une base de données `glop_moveit_db`
2. Créer un utilisateur `glop_user` avec mot de passe `glop_password`
3. Démarrer l'application avec le profil par défaut

### Commandes Maven
```bash
# Compilation
mvn clean compile

# Tests (utilise H2 en mémoire)
mvn test

# Lancement local
mvn spring-boot:run -pl glop-moveit-api
```

## 🐳 Commandes Docker utiles

```bash
# Voir les logs
docker-compose logs -f

# Logs d'un service spécifique
docker-compose logs -f api

# Redémarrer les services
docker-compose restart

# Arrêter tout
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v

# Reconstruire les images
docker-compose build --no-cache

# Voir le statut
docker-compose ps
```

## 📊 Monitoring et Santé

L'application expose des endpoints de monitoring :

- **Health:** http://localhost:8080/actuator/health  
- **Info:** http://localhost:8080/actuator/info
- **Metrics:** http://localhost:8080/actuator/metrics

## 🧪 Tests

### Tests unitaires
```bash
mvn test
```

### Tests d'intégration avec Docker
```bash
# Démarrer les services
docker-compose up -d postgres

# Attendre que PostgreSQL soit prêt
sleep 10

# Lancer les tests d'intégration
mvn integration-test
```

## 🏗️ Extension du projet

### Ajouter un nouveau module

1. Créer le répertoire du module
2. Ajouter dans le `pom.xml` parent :
```xml
<modules>
    <module>glop-moveit-api</module>
    <module>nouveau-module</module>
</modules>
```
3. Créer le `pom.xml` du module
4. Mettre à jour `docker-compose.yml` si nécessaire

### Variables d'environnement Docker

| Variable | Description | Défaut |
|----------|-------------|---------|
| `DB_HOST` | Hôte PostgreSQL | `postgres` |
| `DB_PORT` | Port PostgreSQL | `5432` |
| `DB_NAME` | Nom de la base | `glop_moveit_db` |
| `DB_USERNAME` | Utilisateur DB | `glop_user` |
| `DB_PASSWORD` | Mot de passe DB | `glop_password` |
| `SPRING_PROFILES_ACTIVE` | Profil Spring | `docker` |

## 🔒 Profils de configuration

- **default:** PostgreSQL local
- **docker:** PostgreSQL via Docker avec variables d'environnement
- **test:** H2 en mémoire pour les tests

## 📝 Exemples d'utilisation

### Test de santé
```bash
curl http://localhost:8080/api/users/health
```

### Créer un utilisateur
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com"
  }'
```

### Récupérer tous les utilisateurs
```bash
curl http://localhost:8080/api/users
```