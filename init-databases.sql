-- Script d'initialisation des bases de données pour MoveIt
-- Ce script est exécuté au démarrage du conteneur PostgreSQL

-- Création de la base de données pour le service d'authentification
CREATE DATABASE moveit_auth;

-- Création de la base de données pour le service de notifications
CREATE DATABASE moveit_notification;

-- Vous pouvez ajouter d'autres bases de données ici pour les futurs services
