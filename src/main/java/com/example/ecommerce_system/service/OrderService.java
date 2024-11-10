package com.example.ecommerce_system.service;

import com.example.ecommerce_system.dto.OrderRequest;
import com.example.ecommerce_system.model.Order;
import com.example.ecommerce_system.model.OrderItem;
import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.OrderRepository;
import com.example.ecommerce_system.repository.ProductRepository;
import com.example.ecommerce_system.repository.UserRepository;

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

    public Order placeOrder(User user, OrderRequest orderRequest) {
        Order order = new Order();
        order.setUser(user);

        List<OrderItem> items = orderRequest.getOrderItems().stream()
                .map(itemRequest -> {
                    Product product = productRepository.findById(itemRequest.getProductId()).orElse(null);
                    if (product != null) {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProduct(product);
                        orderItem.setQuantity(itemRequest.getQuantity());
                        orderItem.setPrice(product.getPrice() * itemRequest.getQuantity()); // Calculate price
                        orderItem.setOrder(order); // Link back to order
                        return orderItem;
                    }
                    return null;
                })
                .filter(item -> item != null) // Ensure no null items
                .collect(Collectors.toList());

        order.setOrderItems(items);
        order.setTotalAmount(items.stream().mapToDouble(OrderItem::getPrice).sum()); // Calculate total amount
        // Clear user's cart after placing the order
        user.setCart(Map.of()); // Set an empty map to clear the cart
        userRepository.save(user); // Save updated user to the database
        return orderRepository.save(order);
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
