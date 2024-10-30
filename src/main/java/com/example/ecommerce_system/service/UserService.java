package com.example.ecommerce_system.service;

import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap; // Add this import
import java.util.List;
import java.util.Map; // Add this import
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // Use a HashMap to store reset tokens and associated user IDs
    private final Map<String, Long> resetTokens = new HashMap<>();

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public User signup(User user) {
        user.setRole(User.Role.CUSTOMER); // Automatically assign the CUSTOMER role
        user.setPassword(hashPassword(user.getPassword())); // Hash the password
        return userRepository.save(user); // Save the user
    }

    public Optional<User> login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String hashedPassword = hashPassword(password);
            if (user.get().getPassword().equals(hashedPassword)) {
                return user; // Authentication successful
            }
        }
        return Optional.empty(); // Invalid credentials
    }

    public void updateUser(Long userId, User user) {
        user.setId(userId);
        user.setRole(User.Role.CUSTOMER); // Automatically assign the CUSTOMER role
        user.setPassword(hashPassword(user.getPassword())); // Hash the password
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public void saveResetToken(User user, String resetToken) {
        // Save the reset token along with the user's ID in a suitable way
        resetTokens.put(resetToken, user.getId());
    }

    public Long getUserIdByResetToken(String token) {
        return resetTokens.get(token); // Retrieve the user ID using the reset token
    }

    public void updatePassword(Long userId, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(hashPassword(newPassword));
            userRepository.save(user); // Save the updated user
        }
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
