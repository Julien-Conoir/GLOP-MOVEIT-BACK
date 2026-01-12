package com.moveit.championship.controller;

import com.moveit.championship.entity.Championship;
import com.moveit.championship.exception.ChampionshipNotFoundException;
import com.moveit.championship.service.ChampionshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/championships")
@Tag(name = "Championnats", description = "API de gestion des championnats")
public class ChampionshipController {

    private final ChampionshipService championshipService;

    @Operation(summary = "Récupérer tous les championnats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Championnats récupérés avec succès", content = @Content(schema = @Schema(implementation = Championship.class))),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping
    public ResponseEntity<List<Championship>> getAllChampionships() {
        List<Championship> championships = championshipService.getAllChampionships();
        return ResponseEntity.ok(championships);
    }

    @Operation(summary = "Récupérer un championnat par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Championnat récupéré avec succès", content = @Content(schema = @Schema(implementation = Championship.class))),
            @ApiResponse(responseCode = "404", description = "Championnat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @GetMapping("/{id}")
    public ResponseEntity<Championship> getChampionshipById(@PathVariable Integer id) {
        Championship championship = championshipService.getChampionshipById(id);
        if (championship == null) {
            throw new ChampionshipNotFoundException(id);
        }
        return ResponseEntity.ok(championship);
    }

    @Operation(summary = "Créer un nouveau championnat (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Championnat créé avec succès", content = @Content(schema = @Schema(implementation = Championship.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Championship> createChampionship(@RequestBody Championship championship) {
        Championship createdChampionship = championshipService.createChampionship(championship);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChampionship);
    }

    @Operation(summary = "Mettre à jour un championnat (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Championnat mis à jour avec succès", content = @Content(schema = @Schema(implementation = Championship.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Championnat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Championship> updateChampionship(@PathVariable Integer id, @RequestBody Championship championship) {
        Championship updatedChampionship = championshipService.updateChampionship(id, championship);
        if (updatedChampionship == null) {
            throw new ChampionshipNotFoundException(id);
        }
        return ResponseEntity.ok(updatedChampionship);
    }

    @Operation(summary = "Supprimer un championnat (Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Championnat supprimé avec succès", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle Admin requis", content = @Content()),
            @ApiResponse(responseCode = "404", description = "Championnat non trouvé", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur", content = @Content())
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChampionship(@PathVariable Integer id) {
        Championship championship = championshipService.getChampionshipById(id);
        if (championship == null) {
            throw new ChampionshipNotFoundException(id);
        }
        championshipService.deleteChampionship(id);
        return ResponseEntity.noContent().build();
    }
}