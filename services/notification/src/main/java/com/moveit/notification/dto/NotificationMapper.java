package com.moveit.notification.dto;

import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationRecipient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper pour convertir entre entités et DTOs.
 * Utilise le nouveau modèle notification+recipient.
 */
@Component
public class NotificationMapper {

    /**
     * Convertit un NotificationRecipient (nouveau modèle) en NotificationResponse.
     */
    public NotificationResponse toResponse(NotificationRecipient recipient) {
        Notification notification = recipient.getNotification();
        return NotificationResponse.builder()
                .id(recipient.getId())
                .userId(recipient.getUserId())
                .type(notification.getType())
                .typeName(notification.getType().name())
                .typeDescription(notification.getType().getDescriptionKey())
                .levelName(notification.getLevel().getName())
                .levelPriority(notification.getLevel().getPriority())
                .name(notification.getName())
                .body(notification.getBody())
                .securityId(notification.getSecurityId())
                .competitionId(notification.getCompetitionId())
                .read(recipient.getRead())
                .createdAt(recipient.getCreatedAt())
                .mandatory(notification.getType().isMandatory())
                .build();
    }

    /**
     * Convertit une liste de NotificationRecipients en NotificationListResponse.
     */
    public NotificationListResponse toListResponse(List<NotificationRecipient> recipients, long unreadCount, boolean hasMore) {
        return NotificationListResponse.builder()
                .notifications(recipients.stream()
                        .map(this::toResponse)
                        .toList())
                .unreadCount(unreadCount)
                .hasMore(hasMore)
                .lastNotificationId(recipients.isEmpty() ? null : recipients.get(recipients.size() - 1).getId())
                .build();
    }
}
