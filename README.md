# GLOP MoveIT - Backend 🚀

> Projet multi-module Maven avec API Spring Boot utilisant Java 21, PostgreSQL et Docker

## ⚡ Démarrage rapide

### Avec Docker (recommandé)
```bash
# Windows
start-dev.bat

# Linux/Mac  
chmod +x start-dev.sh && ./start-dev.sh
```

### Accès rapide
- **API:** http://localhost:8080
- **Health Check:** http://localhost:8080/api/users/health
- **pgAdmin:** http://localhost:5050 (avec `--profile admin`)

## 📋 Structure du projet

```
GLOP-MOVEIT-BACK/
├── 🐳 docker-compose.yml          # Orchestration des services
├── 📜 start-dev.sh/.bat           # Scripts de démarrage
├── 🗄️ init-scripts/               # Scripts d'initialisation PostgreSQL
├── 📦 pom.xml                     # Configuration Maven parent
└── 📂 glop-moveit-api/            # Module API Spring Boot
    ├── 🐳 Dockerfile              # Image Docker de l'application
    ├── 📦 pom.xml                 # Dépendances du module API
    ├── 📚 README.md               # Documentation détaillée
    └── 💻 src/                    # Code source
```

## 🛠️ Technologies

| Technologie | Version | Usage |
|-------------|---------|-------|
| **☕ Java** | 21 (LTS) | Langage principal |
| **🍃 Spring Boot** | 3.1.5 | Framework web |
| **🐘 PostgreSQL** | 15 | Base de données |
| **🐳 Docker** | - | Conteneurisation |
| **📦 Maven** | 3.6+ | Gestion des dépendances |

## 🎯 Fonctionnalités

### 🔌 API REST
- ✅ CRUD complet des utilisateurs
- ✅ Validation des données
- ✅ Gestion des erreurs
- ✅ Endpoints de monitoring

### 🏗️ Architecture
- ✅ Multi-module Maven
- ✅ Architecture en couches (Controller/Service/Repository)
- ✅ Profils de configuration (dev/docker/test)
- ✅ Tests unitaires avec H2

### 🐳 Conteneurisation
- ✅ Docker multi-stage pour optimisation
- ✅ Docker Compose pour orchestration
- ✅ PostgreSQL avec persistance
- ✅ pgAdmin pour administration
- ✅ Health checks intégrés

## 🚀 Guide de démarrage

### 1. Prérequis
```bash
# Vérifier Docker
docker --version
docker-compose --version

# Optionnel pour développement local
java --version    # Java 21
mvn --version     # Maven 3.6+
```

### 2. Démarrage des services
```bash
# Cloner le projet
git clone <repo-url>
cd GLOP-MOVEIT-BACK

# Démarrer avec Docker
docker-compose up --build -d

# Ou utiliser les scripts fournis
./start-dev.sh    # Linux/Mac
start-dev.bat     # Windows
```

### 3. Vérification
```bash
# Vérifier les services
docker-compose ps

# Test de l'API
curl http://localhost:8080/api/users/health
```

## 📊 Services et ports

| Service | Port | Accès | Credentials |
|---------|------|-------|-------------|
| **API Spring Boot** | 8080 | http://localhost:8080 | - |
| **PostgreSQL** | 5432 | localhost:5432 | glop_user / glop_password |

## 🧪 Tests et développement

```bash
# Tests unitaires (utilise H2)
mvn test

# Développement local avec PostgreSQL Docker
docker-compose up -d postgres
mvn spring-boot:run -pl glop-moveit-api

# Voir les logs
docker-compose logs -f

# Redémarrage complet
docker-compose down && docker-compose up --build -d
```

## 📖 Documentation détaillée

- **[Documentation complète API](glop-moveit-api/README.md)**
- **[Guide Docker](glop-moveit-api/README.md#-commandes-docker-utiles)**
- **[Architecture et extensions](glop-moveit-api/README.md#-extension-du-projet)**

## 🔧 Configuration

### Variables d'environnement
```env
DB_HOST=postgres
DB_PORT=5432
DB_NAME=glop_moveit_db
DB_USERNAME=glop_user
DB_PASSWORD=glop_password
SPRING_PROFILES_ACTIVE=docker
```

### Profils Spring Boot
- `default` - PostgreSQL local
- `docker` - PostgreSQL via Docker
- `test` - H2 en mémoire

## 🆘 Dépannage

### Problèmes courants
```bash
# Port déjà utilisé
docker-compose down
netstat -tulpn | grep :8080

# Reconstruire les images
docker-compose build --no-cache

# Nettoyer Docker
docker system prune -f

# Logs détaillés
docker-compose logs -f api
```

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Commit (`git commit -am 'Ajouter nouvelle fonctionnalité'`)
4. Push (`git push origin feature/nouvelle-fonctionnalite`)
5. Créer une Pull Request

## 📞 Support

- 📧 Email: support@glop-moveit.com
- 🐛 Issues: [GitHub Issues](../../issues)
- 📚 Wiki: [Documentation Wiki](../../wiki)

---

⭐ **N'oubliez pas de laisser une étoile si ce projet vous aide !**