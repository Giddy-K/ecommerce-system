package com.example.ecommerce_system.repository;

import com.example.ecommerce_system.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
