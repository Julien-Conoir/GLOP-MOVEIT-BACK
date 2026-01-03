# GLOP-MOVEIT-BACK

Backend de l'application Moveit avec architecture microservices.

### Services

- **Auth Service** (port 8082) : Service d'authentification et gestion des utilisateurs
- **Gateway** (port 8080) : API Gateway avec authentification JWT
- **PostgreSQL** (port 5432) : Base de données
- **Prometheus** (port 9090) : Collecte de métriques
- **Loki** (port 3100) : Agrégation de logs
- **Grafana** (port 3000) : Visualisation des métriques et logs

## Prérequis

- Docker
- Docker Compose

## Démarrage

Pour démarrer tous les services :

```bash
docker-compose up -d
```

Pour voir les logs :

```bash
docker-compose logs -f
```

Pour arrêter tous les services :

```bash
docker-compose down
```

Pour reconstruire les images :

```bash
docker-compose up -d --build
```