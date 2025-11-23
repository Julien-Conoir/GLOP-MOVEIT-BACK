package com.moveit.notification.controller;

import com.moveit.notification.dto.*;
import com.moveit.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Crée une nouvelle notification.
     *
     * POST /api/notifications
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("POST /api/notifications - Creating notification for user {}", request.getUserId());
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère les notifications de l'utilisateur connecté avec scroll infini.
     * Le userId est extrait du JWT par la Gateway et passé via header X-User-Id.
     *
     * GET /api/notifications?levelName=CRITIQUE&read=false&limit=20&afterId=123
     */
    @GetMapping
    public ResponseEntity<NotificationListResponse> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String levelName,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long afterId
    ) {
        log.info("GET /api/notifications - Fetching notifications for user {} (levelName={}, read={}, limit={}, afterId={})",
                userId, levelName, read, limit, afterId);

        NotificationListResponse response = notificationService.getUserNotifications(
                userId, levelName, read, limit, afterId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les notifications critiques non lues de l'utilisateur connecté.
     *
     * GET /api/notifications/critical-alerts
     */
    @GetMapping("/critical-alerts")
    public ResponseEntity<List<NotificationResponse>> getMyCriticalAlerts(
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("GET /api/notifications/critical-alerts - Fetching critical alerts for user {}", userId);
        List<NotificationResponse> alerts = notificationService.getCriticalAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Marque une notification comme lue ou non lue.
     *
     * PATCH /api/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody MarkAsReadRequest request
    ) {
        log.info("PATCH /api/notifications/{}/read - Marking as read={} for user {}", id, request.getRead(), userId);
        NotificationResponse response = notificationService.markAsRead(id, userId, request.getRead());
        return ResponseEntity.ok(response);
    }

    /**
     * Marque toutes les notifications de l'utilisateur connecté comme lues.
     *
     * POST /api/notifications/mark-all-read
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("POST /api/notifications/mark-all-read - Marking all as read for user {}", userId);
        int count = notificationService.markAllAsRead(userId);
        log.info("{} notifications marked as read for user {}", count, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Compte les notifications non lues de l'utilisateur connecté (pour le badge).
     *
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.debug("GET /api/notifications/unread-count for user {}", userId);
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Envoyer une notification à plusieurs utilisateurs (Admin uniquement).
     * Supporte envoi ciblé, broadcast via subscriptions, ou broadcast global.
     *
     * POST /api/notifications/send
     * 
     * Sécurité: La méthode sendBroadcast est protégée par @PreAuthorize("hasRole('ADMIN')")
     */
    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody BroadcastNotificationRequest request
    ) {
        log.info("POST /api/notifications/send - Broadcasting notification type:{}, level:{}",
                request.getType(), request.getLevelName());
        
        NotificationResponse response = notificationService.sendBroadcast(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
