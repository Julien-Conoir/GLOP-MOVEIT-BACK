package com.moveit.auth.model;

public record RegisterUserDto(
        String email,
        String password,
        String firstName,
        String surname,
        String phoneNumber,
        boolean acceptsNotifications,
        boolean acceptsLocation
) {}