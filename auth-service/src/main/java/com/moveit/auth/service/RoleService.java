package com.moveit.auth.service;

import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Service de gestion des rôles utilisateurs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Initialise les rôles par défaut au démarrage de l'application.
     */
    @PostConstruct
    void init() {
        Map<RoleEnum, String> roleDescriptionMap = Map.of(
                RoleEnum.SPECTATOR, "Default user role",
                RoleEnum.VOLUNTEER, "Volunteer role",
                RoleEnum.COMMISSIONER, "Commissioner role",
                RoleEnum.ADMIN, "Administrator role"
        );

        roleDescriptionMap.forEach((roleName, description) ->
                roleRepository.findByName(roleName).ifPresentOrElse(
                        role -> log.info("Role already exists: {}", role),
                        () -> {
                            Role roleToCreate = new Role()
                                    .setName(roleName)
                                    .setDescription(description);
                            roleRepository.save(roleToCreate);
                            log.info("Created new role: {}", roleToCreate);
                        }
                )
        );
    }

    /**
     * Recherche un rôle par son nom.
     */
    public Optional<Role> findByName(RoleEnum name) {
        return roleRepository.findByName(name);
    }
}