package com.moveit.auth.config;

import com.moveit.auth.entity.Role;
import com.moveit.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Initialisation des rôles par défaut...");
        createRoleIfNotExists("USER");
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("MODERATOR");
        log.info("Initialisation des rôles terminée");
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role(null, roleName, null);
            roleRepository.save(role);
            log.info("Rôle créé : {}", roleName);
        } else {
            log.debug("Rôle déjà existant : {}", roleName);
        }
    }
}
