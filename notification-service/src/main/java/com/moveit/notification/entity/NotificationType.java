package com.moveit.notification.entity;

public enum NotificationType {
    INCIDENT("Incident notifications"),
    EVENT("Event notifications"),
    SYSTEM("System notifications"),
    MAINTENANCE("Maintenance notifications"),
    ALERT("Urgent alerts");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
