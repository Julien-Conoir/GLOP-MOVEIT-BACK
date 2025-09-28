#!/bin/bash

# Script de démarrage du backend

echo "Demarrage du backend..."

# Arrêter les conteneurs existants
echo "Arret des conteneurs existants..."
docker-compose down

# Construire et démarrer le backend
echo "Construction et demarrage des services..."
docker-compose up --build -d

echo ""
echo "Application accessible sur:"
echo "   - API: http://localhost:8080"
echo "   - Health Check: http://localhost:8080/api/users/health"
echo ""
echo "Commandes utiles:"
echo "   - Voir les logs: docker-compose logs -f"
echo "   - Arreter: docker-compose down"
echo "   - Redemarrer: docker-compose restart"
echo ""