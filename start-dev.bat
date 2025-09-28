@echo off
REM Script de démarrage du backend

echo Demarrage du backend...

REM Arrêter les conteneurs existants
echo Arret des conteneurs existants...
docker-compose down

REM Construire et démarrer le backend
echo Construction et demarrage des services...
docker-compose up --build -d

echo.
echo Application accessible sur:
echo    - API: http://localhost:8080
echo    - Health Check: http://localhost:8080/api/users/health
echo    - pgAdmin: http://localhost:5050 (si activé avec --profile admin)
echo.
echo Commandes utiles:
echo    - Voir les logs: docker-compose logs -f
echo    - Arreter: docker-compose down
echo    - Redemarrer: docker-compose restart
echo.