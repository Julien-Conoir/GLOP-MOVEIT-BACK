package com.moveit.notification.service;

import com.moveit.notification.dto.BroadcastNotificationRequest;
import com.moveit.notification.dto.NotificationListResponse;
import com.moveit.notification.dto.NotificationResponse;

import java.util.List;

/**
 * Interface du service de gestion des notifications.
 */
public interface NotificationService {

    /**
     * Envoie une notification à plusieurs destinataires (Admin uniquement).
     * Utilise le modèle notification+recipient pour éviter la duplication.
     * 
     * Sécurité: Cette méthode doit être appelée uniquement par un admin.
     * 
     * @param request Contenu de la notification et ciblage (userIds, topic, ou broadcast)
     * @return Informations sur l'envoi (notification créée, nombre de recipients)
     */
    NotificationResponse sendBroadcast(BroadcastNotificationRequest request);

    /**
     * Récupère les notifications d'un utilisateur avec scroll infini.
     *
     * @param userId     ID de l'utilisateur
     * @param levelName  Filtre par niveau (optionnel)
     * @param read       Filtre par statut lu/non-lu (optionnel)
     * @param limit      Nombre de notifications à retourner
     * @param afterId    ID de la dernière notification chargée (curseur, optionnel)
     * @return Liste de notifications avec métadonnées
     */
    NotificationListResponse getUserNotifications(Long userId, String levelName, Boolean read, int limit, Long afterId);

    /**
     * Récupère les notifications critiques non lues d'un utilisateur (SLA < 30s).
     *
     * @param userId ID de l'utilisateur
     * @return Liste des notifications critiques non lues
     */
    List<NotificationResponse> getCriticalAlerts(Long userId);

    /**
     * Marque une notification comme lue ou non lue.
     *
     * @param recipientId ID du recipient (notification pour cet utilisateur)
     * @param userId      ID de l'utilisateur (pour vérifier ownership)
     * @param read        Nouveau statut (true = lue, false = non lue)
     * @return La notification mise à jour
     */
    NotificationResponse markAsRead(Long recipientId, Long userId, Boolean read);

    /**
     * Marque toutes les notifications d'un utilisateur comme lues.
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications marquées comme lues
     */
    int markAllAsRead(Long userId);

    /**
     * Compte les notifications non lues d'un utilisateur (pour le badge).
     *
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications non lues
     */
    long countUnreadNotifications(Long userId);
}
