package com.example.ecommerce_system.service;

import com.example.ecommerce_system.dto.OrderRequest;
import com.example.ecommerce_system.model.Order;
import com.example.ecommerce_system.model.OrderItem;
import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.OrderRepository;
import com.example.ecommerce_system.repository.ProductRepository;
import com.example.ecommerce_system.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Order placeOrder(User user, OrderRequest orderRequest) {
        try {
            System.out.println("Placing order for user: " + user.getId());
    
            Order order = new Order();
            order.setUser(user);
    
            List<OrderItem> items = orderRequest.getOrderItems().stream()
                .map(itemRequest -> {
                    Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
                    if (productOpt.isEmpty()) {
                        throw new RuntimeException("Product not found for ID: " + itemRequest.getProductId());
                    }
    
                    Product product = productOpt.get();
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setPrice(product.getPrice() * itemRequest.getQuantity());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.toList());
    
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Order contains no valid items.");
            }
    
            order.setOrderItems(items);
            order.setTotalAmount(items.stream().mapToDouble(OrderItem::getPrice).sum());
            user.setCart(Map.of());  // Assuming cart is cleared after order
    
            // Save the user and order
            userRepository.save(user);
            return orderRepository.save(order);
    
        } catch (Exception e) {
            System.err.println("Error placing order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("An error occurred while placing the order.");
        }
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void updateOrder(Order order) {
        orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
