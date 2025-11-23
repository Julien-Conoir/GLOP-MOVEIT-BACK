package com.moveit.notification.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour créer ou mettre à jour un abonnement aux notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeRequest {

    /**
     * Type de notification (NotificationType.name()) ou NULL pour tous les types.
     */
    @Size(max = 50)
    private String typeName;

    /**
     * Niveau de notification (CRITIQUE, ORGANISATIONNEL, INFORMATIONNEL) ou NULL pour tous.
     */
    @Size(max = 50)
    private String levelName;

    /**
     * Topic custom (ex: "competition:42", "zone:5") ou NULL pour tous.
     */
    @Size(max = 100)
    private String topic;

    /**
     * Active ou inactive (par défaut true lors de la création).
     */
    private Boolean active = true;
}
