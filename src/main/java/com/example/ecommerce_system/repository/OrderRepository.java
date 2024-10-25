package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
