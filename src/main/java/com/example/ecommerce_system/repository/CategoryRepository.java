package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.Category;
import com.example.ecommerce_system.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(CategoryType name);
}
