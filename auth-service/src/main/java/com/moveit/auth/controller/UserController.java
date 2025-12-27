package com.moveit.auth.controller;

import com.moveit.auth.entity.User;
import com.moveit.auth.model.RegisterUserDto;
import com.moveit.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur de gestion des utilisateurs.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Endpoint pour récupérer les informations de l'utilisateur authentifié.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public User authenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    /**
     * Endpoint pour récupérer la liste de tous les utilisateurs.
     */
    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMISSIONER')")
    public List<User> allUsers() {
        return userService.allUsers();
    }

    /**
     * Endpoint pour créer un nouvel administrateur.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createAdministrator(@RequestBody RegisterUserDto registerUserDto) {
        return userService.createAdministrator(registerUserDto);
    }
}