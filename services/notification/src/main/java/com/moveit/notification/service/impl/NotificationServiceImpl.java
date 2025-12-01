package com.moveit.notification.service.impl;

import com.moveit.notification.dto.*;
import com.moveit.notification.entity.Notification;
import com.moveit.notification.entity.NotificationLevel;
import com.moveit.notification.entity.NotificationRecipient;
import com.moveit.notification.repository.NotificationLevelRepository;
import com.moveit.notification.repository.NotificationRecipientRepository;
import com.moveit.notification.repository.NotificationRepository;
import com.moveit.notification.repository.NotificationSubscriptionRepository;
import com.moveit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implémentation du service de gestion des notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final NotificationSubscriptionRepository notificationSubscriptionRepository;
    private final NotificationLevelRepository notificationLevelRepository;
    private final NotificationMapper notificationMapper;

    private static final int BATCH_SIZE = 1000; // Taille des batches pour inserts massifs

    @Override
    @Transactional(readOnly = true)
    public NotificationListResponse getUserNotifications(Long userId, String levelName, Boolean read, int limit, Long afterId) {
        log.debug("Fetching notifications for user {} (levelName={}, read={}, limit={}, afterId={})",
                userId, levelName, read, limit, afterId);

        // Récupérer limit + 1 pour détecter s'il y a plus de résultats
        Pageable pageable = PageRequest.of(0, limit + 1);

        // Récupération des recipients avec filtres
        List<NotificationRecipient> recipients = notificationRecipientRepository.findByUserIdWithFilters(
                userId, levelName, read, afterId, pageable
        ).getContent();

        // Vérifier s'il y a plus de notifications à charger
        boolean hasMore = recipients.size() > limit;

        // Tronquer à la limite demandée
        if (hasMore) {
            recipients = recipients.subList(0, limit);
        }

        // Compter les non lues pour le badge
        long unreadCount = notificationRecipientRepository.countByUserIdAndReadFalse(userId);

        return notificationMapper.toListResponse(recipients, unreadCount, hasMore);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getCriticalAlerts(Long userId) {
        log.debug("Fetching critical alerts for user {}", userId);

        List<NotificationRecipient> criticalRecipients = notificationRecipientRepository.findCriticalUnreadByUserId(userId);

        return criticalRecipients.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long recipientId, Long userId, Boolean read) {
        log.info("Marking recipient {} as read={} for user {}", recipientId, read, userId);

        NotificationRecipient recipient = notificationRecipientRepository.findByIdAndUserId(recipientId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification recipient not found: " + recipientId));

        recipient.setRead(read);
        if (Boolean.TRUE.equals(read)) {
            recipient.setReadAt(java.time.Instant.now());
        } else {
            recipient.setReadAt(null);
        }

        NotificationRecipient updatedRecipient = notificationRecipientRepository.save(recipient);

        return notificationMapper.toResponse(updatedRecipient);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user {}", userId);

        int updatedCount = notificationRecipientRepository.markAllAsReadByUserId(userId);

        log.info("{} notifications marked as read for user {}", updatedCount, userId);
        return updatedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRecipientRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Envoie une notification à plusieurs destinataires (ADMIN UNIQUEMENT).
     * Utilise le nouveau modèle notification+recipient pour éviter la duplication.
     * 
     * Sécurité: Accessible uniquement aux utilisateurs avec le rôle ADMIN.
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")  // ← SÉCURITÉ ADMIN
    public NotificationResponse sendBroadcast(BroadcastNotificationRequest request) {
        log.info("Broadcasting notification - type:{}, level:{}, topic:{}, targetUsers:{}",
                request.getType(), request.getLevelName(), request.getTopic(),
                request.getTargetUserIds() != null ? request.getTargetUserIds().size() : "subscriptions");

        // 1. Créer la notification unique
        NotificationLevel level = notificationLevelRepository.findByName(request.getLevelName())
                .orElseThrow(() -> new IllegalArgumentException("Level not found: " + request.getLevelName()));

        Notification notification = Notification.builder()
                .type(request.getType())
                .level(level)
                .name(request.getName())
                .body(request.getBody())
                .securityId(request.getSecurityId())
                .competitionId(request.getCompetitionId())
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created with ID: {}", notification.getId());

        // 2. Résoudre les destinataires
        Set<Long> userIds = resolveRecipients(request);
        log.info("Resolved {} recipients", userIds.size());

        // 3. Créer les recipients en batch
        int recipientsCreated = createRecipientsInBatch(notification, userIds);
        log.info("Created {} recipient records", recipientsCreated);

        // 4. Retourner le résumé
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .typeName(notification.getType().name())
                .levelName(notification.getLevel().getName())
                .levelPriority(notification.getLevel().getPriority())
                .name(notification.getName())
                .body(notification.getBody())
                .securityId(notification.getSecurityId())
                .competitionId(notification.getCompetitionId())
                .createdAt(notification.getStartDate())
                .mandatory(notification.getType().isMandatory())
                .read(false)
                .build();
    }

    /**
     * Résout la liste des destinataires selon la stratégie de ciblage.
     */
    private Set<Long> resolveRecipients(BroadcastNotificationRequest request) {
        // Si targetUserIds fournis explicitement, les utiliser directement
        if (request.getTargetUserIds() != null && !request.getTargetUserIds().isEmpty()) {
            log.debug("Using explicit target user IDs: {}", request.getTargetUserIds().size());
            return new HashSet<>(request.getTargetUserIds());
        }

        // Si broadcast global, retourner tous les users actifs
        if (Boolean.TRUE.equals(request.getBroadcast())) {
            log.warn("Global broadcast requested - this should be used with caution");
            // TODO: Implémenter getAllActiveUserIds() via un UserService
            throw new UnsupportedOperationException(
                    "Global broadcast not yet implemented - need UserService integration");
        }

        // Sinon, résoudre via subscriptions
        Set<Long> subscribedUsers = notificationSubscriptionRepository.findSubscribedUserIds(
                request.getType().name(),
                request.getLevelName(),
                request.getTopic()
        );

        // Pour les types mandatory, ajouter TOUS les users (ignore subscriptions)
        if (request.getType().isMandatory()) {
            log.info("Type {} is mandatory - should include all users regardless of subscriptions",
                    request.getType());
            // TODO: Ajouter tous les users actifs pour mandatory types
            // subscribedUsers.addAll(userService.getAllActiveUserIds());
        }

        return subscribedUsers;
    }

    /**
     * Crée les recipients en batch pour optimiser les performances.
     */
    private int createRecipientsInBatch(Notification notification, Set<Long> userIds) {
        if (userIds.isEmpty()) {
            log.warn("No recipients to create for notification {}", notification.getId());
            return 0;
        }

        List<NotificationRecipient> allRecipients = new ArrayList<>();
        for (Long userId : userIds) {
            allRecipients.add(new NotificationRecipient(notification, userId));
        }

        // Sauvegarder en batches pour éviter OutOfMemory et optimiser les inserts
        int totalCreated = 0;
        for (int i = 0; i < allRecipients.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, allRecipients.size());
            List<NotificationRecipient> batch = allRecipients.subList(i, end);

            notificationRecipientRepository.saveAll(batch);
            notificationRecipientRepository.flush(); // Force l'écriture en DB

            totalCreated += batch.size();
            log.debug("Saved batch {}/{} - {} recipients",
                    (i / BATCH_SIZE) + 1,
                    (allRecipients.size() / BATCH_SIZE) + 1,
                    batch.size());
        }

        return totalCreated;
    }
}
