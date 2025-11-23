package com.moveit.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO pour retourner un abonnement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private Long id;
    private Long userId;
    private String typeName;
    private String levelName;
    private String topic;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
