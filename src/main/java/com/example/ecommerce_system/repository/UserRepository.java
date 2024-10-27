package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    // This method is provided by JpaRepository to get all users
    @Override
    List<User> findAll(); 
}
