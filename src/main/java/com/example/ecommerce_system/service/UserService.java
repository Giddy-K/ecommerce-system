package com.example.ecommerce_system.service;

import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

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
        // Hash password using SHA-256
        user.setPassword(hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            String hashedPassword = hashPassword(password);
            System.out.println("Hashed input password: " + hashedPassword);
            System.out.println("Stored password: " + user.get().getPassword());
            
            if (user.get().getPassword().equals(hashedPassword)) {
                return user; // Authentication successful
            }
        }
        return Optional.empty(); // Invalid credentials
    }
    

    public void updateUser(Long userId, User user) {
        user.setId(userId);
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public void saveResetToken(User user, String resetToken) {
        // Implement logic to save the reset token in the database associated with the user
    }

    public Long getUserIdByResetToken(String token) {
        // Implement logic to find user by reset token
        return null; // Replace with actual user ID
    }

    public void updatePassword(Long userId, String newPassword) {
        // Implement logic to update user's password
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
