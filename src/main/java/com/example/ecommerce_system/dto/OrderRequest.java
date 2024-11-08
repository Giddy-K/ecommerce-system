package com.example.ecommerce_system.dto;

import java.util.List;

public class OrderRequest {
    private List<OrderItemRequest> orderItems; // List of product ID and quantity pairs

    public List<OrderItemRequest> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemRequest> orderItems) {
        this.orderItems = orderItems;
    }
}