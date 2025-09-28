package com.glop.moveit.api.service;

import com.glop.moveit.api.model.User;
import com.glop.moveit.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère un utilisateur par son ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Crée un nouvel utilisateur
     */
    public User createUser(User user) {
        // Vérification que le nom d'utilisateur et l'email sont uniques
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Le nom d'utilisateur existe déjà");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("L'email existe déjà");
        }
        
        return userRepository.save(user);
    }

    /**
     * Met à jour un utilisateur existant
     */
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());

        return userRepository.save(user);
    }

    /**
     * Supprime un utilisateur
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + id));
        
        userRepository.delete(user);
    }
}