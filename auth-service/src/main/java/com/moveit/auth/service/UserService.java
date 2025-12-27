package com.moveit.auth.service;

import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.entity.User;
import com.moveit.auth.repository.UserRepository;
import com.moveit.auth.model.RegisterUserDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des utilisateurs.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Initialise le super administrateur au démarrage de l'application.
     */
    @PostConstruct
    void init(){
        RegisterUserDto userDto = new RegisterUserDto("admin@email.com","123456","Admin","User", null, false, false);

        Optional<Role> optionalRole = roleService.findByName(RoleEnum.ADMIN);
        Optional<User> optionalUser = userRepository.findByEmail(userDto.email());

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        var user = new User()
                .setFirstName(userDto.firstName())
                .setSurname(userDto.surname())
                .setEmail(userDto.email())
                .setPassword(passwordEncoder.encode(userDto.password()))
                .setRole(optionalRole.get())
                .setAcceptsNotifications(userDto.acceptsNotifications())
                .setAcceptsLocation(userDto.acceptsLocation());

        userRepository.save(user);
    }

    /**
     * Récupère la liste de tous les utilisateurs.
     */
    public List<User> allUsers() {
        return userRepository.findAll();
    }

    /**
     * Crée un nouvel administrateur.
     */
    public User createAdministrator(RegisterUserDto input) {
        Optional<Role> optionalRole = roleService.findByName(RoleEnum.ADMIN);

        if (optionalRole.isEmpty()) {
            return null;
        }

        var user = new User()
                .setFirstName(input.firstName())
                .setSurname(input.surname())
                .setEmail(input.email())
                .setPassword(passwordEncoder.encode(input.password()))
                .setPhoneNumber(input.phoneNumber())
                .setRole(optionalRole.get())
                .setAcceptsNotifications(input.acceptsNotifications())
                .setAcceptsLocation(input.acceptsLocation());

        return userRepository.save(user);
    }
}