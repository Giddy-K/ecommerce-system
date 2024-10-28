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

import java.util.List;
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
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password,
            HttpServletResponse response) {
        try {
            System.out.println("Login attempt for email: " + email);

            Optional<User> user = userService.login(email, password);
            if (user.isPresent()) {
                String token = jwtUtil.generateToken(email);
                Cookie cookie = new Cookie("JWT", token);
                cookie.setHttpOnly(true);
                cookie.setMaxAge(3600); // 1 hour
                response.addCookie(cookie);
                return ResponseEntity.ok("Login successful: " + user.get().getName());
            }

            System.out.println("Invalid credentials for email: " + email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login.");
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable Long userId, @RequestBody User user) {
        userService.updateUser(userId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            Optional<User> userOptional = userService.getUserByEmail(email);
            if (userOptional.isPresent()) {
                String resetToken = UUID.randomUUID().toString();
                userService.saveResetToken(userOptional.get(), resetToken); // Ensure this method is implemented correctly
                String resetLink = "http://localhost:8080/api/users/reset-password?token=" + resetToken;
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Password Reset Request");
                message.setText("To reset your password, click the link below:\n" + resetLink);
                mailSender.send(message);
                return ResponseEntity.ok("Password reset link sent to your email.");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the password reset process.");
        }
    }    

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Long userId = userService.getUserIdByResetToken(token);
        if (userId != null) {
            userService.updatePassword(userId, newPassword); // Call updatePassword directly
            return ResponseEntity.ok("Password reset successful");
        }
        return ResponseEntity.badRequest().body("Invalid or expired reset token");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // For logout, you can implement token blacklist logic if needed.
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminEndpoint(@RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractEmail(token.substring(7)); // Remove "Bearer "
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent() && user.get().getRole() == User.Role.ADMIN) {
            return ResponseEntity.ok("Admin access granted");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    // Implement a method to hash passwords (use bcrypt or similar)
    private String hashPassword(String password) {
        // Implement password hashing logic, e.g., using BCryptPasswordEncoder
        return password; // Replace with actual hashed password
    }
}
