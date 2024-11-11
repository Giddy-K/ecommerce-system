package com.example.ecommerce_system.service;

import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.UserRepository;
import com.example.ecommerce_system.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Using a HashMap to store reset tokens and associated user IDs
    private final Map<String, Long> resetTokens = new HashMap<>();

    //hash password functtion [TODO]
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
                return user; // If authentication is successful
            }
        }
        return Optional.empty(); // Invalid credentials
    }

    public void updateUser(User updatedUser) {
        // Fetch the existing user from the database
        Optional<User> existingUserOpt = userRepository.findById(updatedUser.getId());
        
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
    
            // Update only the necessary fields
            existingUser.setName(updatedUser.getName());
            existingUser.setRole(User.Role.CUSTOMER); // Keep the role as CUSTOMER [TODO]
    
            // Update the password only if it's provided in the request
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(hashPassword(updatedUser.getPassword())); // Hash the password
            }
    
            // Save the updated user
            userRepository.save(existingUser);
        }
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

    public Optional<User> getUserFromToken(String token) {
        String email = jwtUtil.extractEmail(token.substring(7)); // Remove "Bearer " prefix
        return userRepository.findByEmail(email);
    }

    public Long extractUserId(String token) {
        String email = jwtUtil.extractEmail(token);
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(User::getId).orElse(null);
    }

    public void addToCart(User user, Long productId) {
        Map<Long, Integer> cartItems = user.getCart();  // Get the cart (Map<Long, Integer>)
        cartItems.put(productId, cartItems.getOrDefault(productId, 0) + 1);  // Add item or update quantity
        userRepository.save(user);  // Save the updated user
    }

    public void removeFromCart(User user, Long productId) {
        Map<Long, Integer> cartItems = user.getCart();  // Get the cart (Map<Long, Integer>)
        cartItems.remove(productId);  // Remove the item from the cart
        userRepository.save(user);  // Save the updated user
    }
    
    public boolean updateCartQuantity(User user, Long productId, int quantityChange) {
        Map<Long, Integer> cartItems = user.getCart();  // Assuming user.getCart() returns Map<Long, Integer>
        int currentQuantity = cartItems.getOrDefault(productId, 0);
        
        if (currentQuantity + quantityChange <= 0) {
            cartItems.remove(productId);  // Remove the product if quantity becomes 0 or negative
            userRepository.save(user);  // Save the updated user
            return true; // Indicating the item was removed
        } else {
            cartItems.put(productId, currentQuantity + quantityChange);  // Update the quantity of the product
            userRepository.save(user);  // Save the updated user
            return false; // Indicating the item quantity was updated
        }
    }
    
}
