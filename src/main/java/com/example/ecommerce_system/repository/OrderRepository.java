package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId); // Derived query to find orders by user ID
}
