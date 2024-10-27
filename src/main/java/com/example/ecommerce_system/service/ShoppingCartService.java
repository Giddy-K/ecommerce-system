package com.example.ecommerce_system.service;

import com.example.ecommerce_system.model.CartItem;
import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.model.ShoppingCart;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.ProductRepository;
import com.example.ecommerce_system.repository.ShoppingCartRepository;
import com.example.ecommerce_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShoppingCartService {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private UserRepository userRepository; // Inject UserRepository

    @Autowired
    private ProductRepository productRepository;

    public ShoppingCart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ShoppingCart cart = new ShoppingCart(user);
        return shoppingCartRepository.save(cart);
    }
    

    public Optional<ShoppingCart> getCartByUserId(Long userId) {
        return shoppingCartRepository.findByCustomer_Id(userId); // Use the custom query method
    }

    public ShoppingCart addItemToCart(Long cartId, Long productId, Integer quantity) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    
        // Fetch the product based on productId
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    
        // Create a new CartItem and set its product and quantity
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(product.getPrice() * quantity);
    
        // Add item to cart and save
        cart.addItem(cartItem);
        return shoppingCartRepository.save(cart);
    }
    


    public ShoppingCart removeItemFromCart(Long cartId, Long itemId) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));
        cart.removeItem(itemToRemove);
        return shoppingCartRepository.save(cart);
    }

    public double getTotalPrice(Long cartId) {
        ShoppingCart cart = shoppingCartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return cart.getTotalPrice();
    }
}
