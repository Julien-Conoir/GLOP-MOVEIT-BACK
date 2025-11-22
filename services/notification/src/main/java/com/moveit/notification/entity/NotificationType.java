package com.moveit.notification.entity;

import lombok.Getter;

/**
 * Types de notifications avec clé i18n et flag mandatory.
 * Enum Java pour éviter table de référence inutile.
 */
@Getter
public enum NotificationType {
    
    // Types pour athlètes (essentiels)
    ATHLETE_SUMMONS("notification.type.athlete_summons", true),
    EVENT_INCIDENT("notification.type.event_incident", true),
    SCHEDULE_CHANGE("notification.type.schedule_change", true),
    
    // Types pour commissaires (essentiels)
    ZONE_INCIDENT("notification.type.zone_incident", true),
    VALIDATION_REQUEST("notification.type.validation_request", true),
    ASSIGNMENT_CHANGE("notification.type.assignment_change", true),
    
    // Types pour volontaires (essentiels)
    MISSION_NOTIFICATION("notification.type.mission_notification", true),
    PLANNING_CHANGE("notification.type.planning_change", true),
    
    // Types critiques sécurité (essentiels)
    SECURITY_INCIDENT("notification.type.security_incident", true),
    EVACUATION_ALERT("notification.type.evacuation_alert", true),
    EMERGENCY_ALERT("notification.type.emergency_alert", true),
    
    // Types non essentiels (désactivables)
    GENERAL_INFO("notification.type.general_info", false),
    REMINDER("notification.type.reminder", false),
    UPDATE("notification.type.update", false);

    private final String descriptionKey;  // Clé i18n
    private final boolean mandatory;

    NotificationType(String descriptionKey, boolean mandatory) {
        this.descriptionKey = descriptionKey;
        this.mandatory = mandatory;
    }
}
