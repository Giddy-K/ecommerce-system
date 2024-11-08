package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.dto.OrderRequest;
import com.example.ecommerce_system.model.Order;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.service.UserService;
import com.example.ecommerce_system.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID; // For generating reset tokens

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender; // For sending emails

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody User user) {
        User newUser = userService.signup(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials,
            HttpServletResponse response) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        try {
            System.out.println("Login attempt for email: " + email);
            Optional<User> user = userService.login(email, password);
            if (user.isPresent()) {
                // Generate token with claims
                String token = jwtUtil.generateTokenWithClaims(email,
                        Map.of("role", user.get().getRole(), "name", user.get().getName()));

                // Set JWT as a secure HttpOnly cookie with SameSite attribute
                Cookie cookie = new Cookie("JWT", token);
                cookie.setHttpOnly(true);
                cookie.setSecure(true); // Send only over HTTPS
                cookie.setMaxAge(3600); // 1 hour
                cookie.setPath("/");
                response.addCookie(cookie);

                // Prepare JSON response
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "Login successful");
                responseBody.put("username", user.get().getName());
                responseBody.put("token", token); // Return the token
                responseBody.put("role", user.get().getRole().toString()); // Include user role
                return ResponseEntity.ok(responseBody);
            }

            System.out.println("Invalid login attempt for email: " + email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            System.out.println("An error occurred during login for email: " + email + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            Optional<User> userOptional = userService.getUserByEmail(email);
            if (userOptional.isPresent()) {
                String resetToken = UUID.randomUUID().toString();
                userService.saveResetToken(userOptional.get(), resetToken);
                String resetLink = "http://localhost:8084/api/users/reset-password?token=" + resetToken;
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Password Reset Request");
                message.setText("To reset your password, click the link below:\n" + resetLink);
                mailSender.send(message);
                return ResponseEntity.ok("Password reset link sent to your email.");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during the password reset process.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Long userId = userService.getUserIdByResetToken(token);
        if (userId != null) {
            userService.updatePassword(userId, newPassword);
            return ResponseEntity.ok("Password reset successful");
        }
        return ResponseEntity.badRequest().body("Invalid or expired reset token");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Clear cookie
        response.addCookie(cookie);

        return ResponseEntity.ok("Logout successful");
    }

    // @GetMapping("/{userId}")
    // public ResponseEntity<?> getUserById(@PathVariable Long userId) {
    // Optional<User> user = userService.getUserById(userId);
    // return user.map(ResponseEntity::ok)
    // .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    // }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        // Assuming the token service can extract userId from the token
        Long userId = userService.extractUserId(token.replace("Bearer ", ""));
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody User user,
            @RequestHeader("Authorization") String token) {
        if (isUserAdmin(token)) {
            userService.updateUser(userId, user);
            return ResponseEntity.ok("User updated successfully.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin access required.");
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        if (isUserAdmin(token)) {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin access required.");
    }

    @GetMapping("/")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        if (isUserAdmin(token)) {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin access required.");
    }

    // @GetMapping("/admin")
    // public ResponseEntity<String> adminEndpoint(@RequestHeader("Authorization")
    // String token) {
    // if (isUserAdmin(token)) {
    // return ResponseEntity.ok("Admin access granted");
    // }
    // return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    // }

    @GetMapping("/admin")
    public boolean isUserAdmin(String token) {
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String email = jwtUtil.extractEmail(token);
        Optional<User> user = userService.getUserByEmail(email);
        return user.isPresent() && user.get().getRole() == User.Role.ADMIN;
    }

    @PostMapping("/add-to-cart")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Long> request,
            @RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isPresent()) {
            userService.addToCart(user.get(), request.get("productId"));
            return ResponseEntity.ok("Product added to cart.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
    }

    @GetMapping("/cart")
    public ResponseEntity<?> getCartItems(@RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isPresent()) {
            List<Long> cart = user.get().getCart(); // Assuming cart is a list of product IDs
            Map<Long, Integer> cartItems = new HashMap<>();
            for (Long productId : cart) {
                cartItems.put(productId, cartItems.getOrDefault(productId, 0) + 1);
            }
            return ResponseEntity.ok(cartItems); // Return the cart as a map of product IDs and quantities
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
    }

    @DeleteMapping("/remove-from-cart/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId,
            @RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isPresent()) {
            userService.removeFromCart(user.get(), productId);
            return ResponseEntity.ok("Product removed from cart.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
    }

    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest,
            @RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isPresent()) {
            Order order = userService.placeOrder(user.get(), orderRequest);
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
    }

    @GetMapping("/orders/me")
    public ResponseEntity<?> getUserOrders(@RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isPresent()) {
            List<Order> orders = userService.getUserOrders(user.get().getId());
            return ResponseEntity.ok(orders);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
    }

}
