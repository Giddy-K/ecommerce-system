package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByCustomer_Id(Long customerId);
}
