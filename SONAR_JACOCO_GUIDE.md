# Guide SonarQube et JaCoCo

## Installation et démarrage

### 1. Démarrer SonarQube
```bash
docker-compose up -d sonarqube sonar-postgres
```

### 2. Accéder à SonarQube
- URL: http://localhost:9000
- Identifiant par défaut: `admin`
- Mot de passe par défaut: `admin`
- **Important**: Lors de la première connexion, SonarQube vous demandera de changer le mot de passe par défaut en `admin123` (configuré dans le pom.xml)

### 3. Créer un token d'authentification (première fois uniquement)
1. Se connecter à SonarQube
2. Aller dans **My Account** > **Security** > **Generate Token**
3. Nom du token: `moveit-analysis`
4. Type: `Global Analysis Token`
5. Copier le token généré

## Utilisation

### Exécuter les tests avec couverture JaCoCo
```bash
mvn clean test
```

Les rapports JaCoCo seront générés dans:
- `auth-service/target/site/jacoco/index.html`
- `gateway/target/site/jacoco/index.html`

### Analyser le code avec SonarQube (avec token)
```bash
mvn clean verify sonar:sonar \
  -Dsonar.token=VOTRE_TOKEN_ICI
```

### Analyser le code avec SonarQube (avec login/password)
```bash
mvn clean verify sonar:sonar \
  -Dsonar.login=admin \
  -Dsonar.password=admin123
```

### Générer uniquement les rapports JaCoCo
```bash
mvn jacoco:report
```

## Configuration

### Propriétés SonarQube (pom.xml)
- `sonar.host.url`: http://localhost:9000
- `sonar.login`: admin
- `sonar.password`: admin123
- Couverture minimale requise: 50%

### Règles de qualité JaCoCo
- Couverture minimum par package: 50% de lignes
- Le build échouera si la couverture est insuffisante (désactiver avec `-Djacoco.skip=true`)

## Commandes utiles

### Analyser un module spécifique
```bash
cd auth-service
mvn clean verify sonar:sonar -Dsonar.token=VOTRE_TOKEN_ICI
```

### Désactiver la vérification JaCoCo
```bash
mvn clean test -Djacoco.skip=true
```

### Voir les rapports dans le navigateur
```bash
# JaCoCo
open auth-service/target/site/jacoco/index.html
open gateway/target/site/jacoco/index.html

# SonarQube
open http://localhost:9000
```

## Métriques surveillées

### SonarQube
- Bugs
- Vulnérabilités
- Code Smells
- Couverture de code
- Duplication de code
- Dette technique

### JaCoCo
- Couverture des lignes
- Couverture des branches
- Couverture des méthodes
- Couverture des classes

## Troubleshooting

### SonarQube ne démarre pas
```bash
# Vérifier les logs
docker logs sonarqube

# Augmenter la mémoire disponible (si nécessaire)
docker-compose down
# Modifier vm.max_map_count sur Linux
sudo sysctl -w vm.max_map_count=262144
docker-compose up -d sonarqube
```

### Erreur d'authentification SonarQube
- Vérifier que le mot de passe a été changé en `admin123`
- Ou générer et utiliser un token d'authentification

### Les tests ne génèrent pas de rapport
```bash
# Nettoyer et reconstruire
mvn clean install
mvn test
```
