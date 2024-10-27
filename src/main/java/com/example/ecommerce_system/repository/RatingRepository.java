package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByProductId(Long productId);
}
