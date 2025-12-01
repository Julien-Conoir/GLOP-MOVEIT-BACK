package com.moveit.auth.service;

import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
import com.moveit.auth.repository.UserRepository;
import com.moveit.auth.model.LoginUserDto;
import com.moveit.auth.model.RegisterUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service de gestion de l'authentification.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Inscrit un nouvel utilisateur.
     */
    public User signup(RegisterUserDto input) {

        Optional<Role> optionalRole = roleService.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return null;
        }

        var user = new User()
                .setFullName(input.fullName())
                .setEmail(input.email())
                .setPassword(passwordEncoder.encode(input.password()))
                .setRole(optionalRole.get());

        return userRepository.save(user);
    }

    /**
     * Authentifie un utilisateur.
     */
    public UserDetails authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.email(),
                        input.password()
                )
        );

        return userRepository.findByEmail(input.email())
                .orElseThrow();
    }
}