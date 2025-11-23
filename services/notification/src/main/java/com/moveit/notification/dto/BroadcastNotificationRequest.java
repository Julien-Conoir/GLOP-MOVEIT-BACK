package com.moveit.notification.dto;

import com.moveit.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO pour envoyer une notification à plusieurs destinataires (admin).
 * Supporte envoi ciblé (userIds) ou broadcast via subscriptions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastNotificationRequest {

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Level name is required")
    private String levelName; // CRITIQUE, ORGANISATIONNEL, INFORMATIONNEL

    @NotBlank(message = "Notification title is required")
    private String name;

    private String body;

    private Integer securityId;

    private Integer competitionId;

    /**
     * Topic pour ciblage via subscriptions (optionnel).
     * Ex: "competition:42" envoie aux abonnés de ce topic.
     */
    private String topic;

    /**
     * Liste explicite d'userIds (optionnel).
     * Si fournie, envoie uniquement à ces users (ignore subscriptions).
     * Si NULL, résout les destinataires via subscriptions.
     */
    private Set<Long> targetUserIds;

    /**
     * Si true, envoie à TOUS les users actifs (ignore subscriptions).
     * Utiliser avec précaution pour les annonces globales.
     */
    private Boolean broadcast = false;
}
