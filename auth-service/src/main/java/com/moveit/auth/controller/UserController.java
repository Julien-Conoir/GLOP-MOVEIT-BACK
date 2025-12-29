package com.moveit.auth.controller;

import com.moveit.auth.entity.User;
import com.moveit.auth.model.RegisterUserDto;
import com.moveit.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Récupération de l'utilisateur authentifié")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Récupération de l'utilisateur authentifié avec succès", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Utilisateur non authentifié"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public User authenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Operation(summary = "Récupération de tous les utilisateurs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Récupération de tous les utilisateurs avec succès", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMISSIONER')")
    public List<User> allUsers() {
        return userService.allUsers();
    }

    @Operation(summary = "Création d'un administrateur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Administrateur créé avec succès", content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Données d'inscription invalides"),
            @ApiResponse(responseCode = "409", description = "L'utilisateur existe déjà"),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createAdministrator(@RequestBody RegisterUserDto registerUserDto) {
        return userService.createAdministrator(registerUserDto);
    }
}