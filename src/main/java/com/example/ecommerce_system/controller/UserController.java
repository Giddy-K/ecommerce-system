package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.service.UserService;
import com.example.ecommerce_system.util.JwtUtil;

import jakarta.mail.internet.MimeMessage; // For sending emails if needed
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
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password,
            HttpServletResponse response) {
        try {
            System.out.println("Login attempt for email: " + email);
            // logger.info("Login attempt for email: {}", email);

            // Authenticate user
            Optional<User> user = userService.login(email, password);
            if (user.isPresent()) {
                // Generate token with claims
                String token = jwtUtil.generateTokenWithClaims(email, Map.of("role", user.get().getRole()));

                // Set JWT as a secure HttpOnly cookie with SameSite attribute
                Cookie cookie = new Cookie("JWT", token);
                cookie.setHttpOnly(true);
                cookie.setSecure(true); // Send only over HTTPS
                cookie.setMaxAge(3600); // 1 hour
                cookie.setPath("/");
                response.addCookie(cookie);

                // Add JWT to Authorization header
                response.setHeader("Authorization", "Bearer " + token);

                // Prepare JSON response
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("message", "Login successful");
                responseBody.put("username", user.get().getName());
                return ResponseEntity.ok(responseBody);
            }

            // Log failed attempt with generic message
            System.out.println("Invalid login attempt for email: " + email);
            // logger.warn("Invalid login attempt for email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (Exception e) {
            System.out.println("An error occurred during login for email: {}" + email + e);
            // logger.error("An error occurred during login for email: {}", email, e);
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

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        if (isUserAdmin(token)) {
            Optional<User> user = userService.getUserById(userId);
            return user.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Admin access required.");
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

    @GetMapping("/admin")
    public ResponseEntity<String> adminEndpoint(@RequestHeader("Authorization") String token) {
        if (isUserAdmin(token)) {
            return ResponseEntity.ok("Admin access granted");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    private boolean isUserAdmin(String token) {
        String email = jwtUtil.extractEmail(token.substring(7)); // Remove "Bearer " prefix
        Optional<User> user = userService.getUserByEmail(email);
        return user.isPresent() && user.get().getRole() == User.Role.ADMIN;
    }

    private String hashPassword(String password) {
        // Implement password hashing logic, e.g., using BCryptPasswordEncoder
        return password; // Replace with actual hashed password
    }
}
