package com.example.ecommerce_system.dto;

import java.util.Map;

public class OrderRequest {
    private Map<Long, Integer> productQuantities; // Map of product IDs and their quantities

    public Map<Long, Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Map<Long, Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }
}
