package com.moveit.auth.model;

public record RegisterUserDto(String email, String password, String fullName) {}