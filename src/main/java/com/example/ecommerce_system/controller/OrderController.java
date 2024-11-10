package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.dto.OrderRequest;
import com.example.ecommerce_system.dto.OrderStatusUpdateRequest;
import com.example.ecommerce_system.model.Order;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.service.OrderService;
import com.example.ecommerce_system.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    // PLACE AN ORDER
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest,
            @RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
        }
        Order order = orderService.placeOrder(user.get(), orderRequest);
        if (order.getOrderItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Order contains invalid or unavailable products.");
        }
        return ResponseEntity.ok(order);
    }

    // GET THE USERS ORDERS
    @GetMapping("/me")
    public ResponseEntity<?> getUserOrders(@RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isPresent()) {
            List<Order> orders = orderService.getUserOrders(user.get().getId());
            return ResponseEntity.ok(orders);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
    }

    //GET A SPECIFIC ORDER WITH DETAILS
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //GET ALL THE ORDERS
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    //UPDATE ORDER STATUS
    @PutMapping("/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        Optional<Order> orderOptional = orderService.getOrderById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(request.getStatus()); // Update the status
            orderService.updateOrder(order); // Save updated order
            return ResponseEntity.ok("Order status updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found.");
        }
    }

    //DELETE AN ORDER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
