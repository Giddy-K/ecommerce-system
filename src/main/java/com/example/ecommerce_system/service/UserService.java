package com.example.ecommerce_system.service;

import com.example.ecommerce_system.dto.OrderRequest;
import com.example.ecommerce_system.model.Order;
import com.example.ecommerce_system.model.OrderItem;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.OrderRepository;
import com.example.ecommerce_system.repository.ProductRepository;
import com.example.ecommerce_system.repository.UserRepository;
import com.example.ecommerce_system.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap; // Add this import
import java.util.List;
import java.util.Map; // Add this import
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrderRepository orderRepository; // To manage user orders

    @Autowired
    private ProductRepository productRepository;

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
        user.getCart().add(productId); // Assuming cart is a List<Long> in User model
        userRepository.save(user);
    }

    public void removeFromCart(User user, Long productId) {
        user.getCart().remove(productId); // Assuming cart is a List<Long> in User model
        userRepository.save(user);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId); // Assuming Order has a userId field
    }

    public Order placeOrder(User user, OrderRequest orderRequest) {
        Order order = new Order();
        order.setUser(user);

        // Assuming each entry in productQuantities is a product ID and quantity
        List<OrderItem> items = orderRequest.getProductQuantities().entrySet().stream()
                .map(entry -> {
                    OrderItem item = new OrderItem();
                    item.setProduct(productRepository.findById(entry.getKey()).orElse(null));
                    item.setQuantity(entry.getValue());
                    return item;
                }).collect(Collectors.toList());

        order.setOrderItems(items);
        return orderRepository.save(order);
    }
}
