package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    // Add custom methods if needed, for example, finding by customer
}
