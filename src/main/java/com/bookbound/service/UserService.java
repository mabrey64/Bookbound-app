package com.bookbound.service;

import com.bookbound.model.User;
import com.bookbound.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for User entity business logic.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Create a new user.
     * @param user the user to create
     * @return the created user
     * @throws IllegalArgumentException if email already exists
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        return userRepository.save(user);
    }
    
    /**
     * Get user by ID.
     * @param id the user ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user by email.
     * @param email the email address
     * @return Optional containing the user if found
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get all users.
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Search users by name.
     * @param name the name fragment to search for
     * @return list of matching users
     */
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Update user information.
     * @param id the user ID
     * @param updatedUser the updated user data
     * @return the updated user
     * @throws IllegalArgumentException if user not found or email conflict
     */
    public User updateUser(String id, User updatedUser) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Check email uniqueness if email is being changed
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email " + updatedUser.getEmail() + " is already in use");
        }
        
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        
        return userRepository.save(existingUser);
    }
    
    /**
     * Delete user by ID.
     * @param id the user ID
     * @throws IllegalArgumentException if user not found
     */
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    /**
     * Check if user exists by ID.
     * @param id the user ID
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String id) {
        return userRepository.existsById(id);
    }
} 