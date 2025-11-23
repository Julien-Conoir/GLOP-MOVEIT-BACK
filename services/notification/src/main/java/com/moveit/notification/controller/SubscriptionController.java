package com.moveit.notification.controller;

import com.moveit.notification.dto.SubscribeRequest;
import com.moveit.notification.dto.SubscriptionResponse;
import com.moveit.notification.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des abonnements aux notifications.
 */
@RestController
@RequestMapping("/api/notifications/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * S'abonner à des notifications.
     * POST /api/notifications/subscriptions
     */
    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SubscribeRequest request) {

        log.info("POST /api/notifications/subscriptions - User {} subscribing", userId);
        SubscriptionResponse response = subscriptionService.subscribe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulter ses abonnements.
     * GET /api/notifications/subscriptions
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions(
            @RequestHeader("X-User-Id") Long userId) {

        log.info("GET /api/notifications/subscriptions - User {}", userId);
        List<SubscriptionResponse> subscriptions = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Se désabonner (soft delete - désactive la subscription).
     * PATCH /api/notifications/subscriptions/{id}/unsubscribe
     */
    @PatchMapping("/{id}/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        log.info("PATCH /api/notifications/subscriptions/{}/unsubscribe - User {}", id, userId);
        subscriptionService.unsubscribe(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Supprimer définitivement une subscription (hard delete).
     * DELETE /api/notifications/subscriptions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        log.info("DELETE /api/notifications/subscriptions/{} - User {}", id, userId);
        subscriptionService.deleteSubscription(userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gestion des erreurs métier.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
