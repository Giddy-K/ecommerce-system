package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.dto.OrderItemRequest;
import com.example.ecommerce_system.dto.OrderRequest;
import com.example.ecommerce_system.dto.OrderStatusUpdateRequest;
import com.example.ecommerce_system.model.Order;
import com.example.ecommerce_system.model.OrderItem;
import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.OrderRepository;
import com.example.ecommerce_system.repository.ProductRepository;
import com.example.ecommerce_system.repository.UserRepository;
import com.example.ecommerce_system.service.OrderService;
import com.example.ecommerce_system.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // PLACE AN ORDER
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest) {
        // Retrieve the user from the database using userId from OrderRequest
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create Order instance and populate it with data from OrderRequest
        Order order = new Order();
        order.setUser(user);
        order.setAddress(orderRequest.getAddress());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setOrderDate(LocalDateTime.now());

        // Iterate over each OrderItemRequest to populate OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order); // Set the order reference in each OrderItem

            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);

        // Save the order to the database
        orderRepository.save(order);

        return ResponseEntity.ok("Order placed successfully");
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

    // GET A SPECIFIC ORDER WITH DETAILS
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET ALL THE ORDERS
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // UPDATE ORDER STATUS
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

    // DELETE AN ORDER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
