package com.moveit.auth.service;

import com.moveit.auth.dto.*;
import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.User;
import com.moveit.auth.repository.RoleRepository;
import com.moveit.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));
        return user;
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateJwtToken(authentication);

        User user = (User) authentication.getPrincipal();
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserDto userDto = convertToUserDto(user);

        return new JwtResponse(jwt, userDto);
    }

    public UserDto registerUser(RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Erreur : Le nom d'utilisateur est déjà pris !");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Erreur : L'email est déjà utilisé !");
        }

        // Créer le nouveau compte utilisateur
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Erreur : Rôle non trouvé."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role foundRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new RuntimeException("Erreur : Rôle " + role + " non trouvé."));
                roles.add(foundRole);
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        return convertToUserDto(savedUser);
    }

    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            return convertToUserDto(user);
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }

    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setLastLogin(user.getLastLogin());
        userDto.setEnabled(user.isEnabled());

        // Conversion des rôles en RoleDto pour éviter la sérialisation circulaire
        Set<RoleDto> roleDtos = user.getRoles().stream()
                .map(role -> new RoleDto(role.getId(), role.getName()))
                .collect(java.util.stream.Collectors.toSet());
        userDto.setRoles(roleDtos);

        return userDto;
    }
}