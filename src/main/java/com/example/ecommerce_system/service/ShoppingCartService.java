package com.example.ecommerce_system.service;

import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.model.ShoppingCart;
import com.example.ecommerce_system.repository.ProductRepository;
import com.example.ecommerce_system.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductRepository productRepository;

    public ShoppingCart addToCart(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ShoppingCart cart = shoppingCartRepository.findById(1L) // For now, we use a hardcoded cart ID (or customer ID)
                .orElse(new ShoppingCart()); // Create a new cart if it doesn't exist

        cart.addProduct(product); // Add the product to the cart

        return shoppingCartRepository.save(cart); // Save the updated cart
    }

    public List<ShoppingCart> getAllCarts() {
        return shoppingCartRepository.findAll();
    }

    public ShoppingCart getCartById(Long id) {
        return shoppingCartRepository.findById(id).orElse(null);
    }

    public ShoppingCart createCart(ShoppingCart cart) {
        return shoppingCartRepository.save(cart);
    }

    public void deleteCart(Long id) {
        shoppingCartRepository.deleteById(id);
    }
}
