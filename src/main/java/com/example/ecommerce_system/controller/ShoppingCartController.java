package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.model.CartItem;
import com.example.ecommerce_system.model.ShoppingCart;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/create")
    public ResponseEntity<ShoppingCart> createCart(@RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        ShoppingCart cart = shoppingCartService.createCart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }
    

    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingCart> getCart(@PathVariable Long userId) {
        Optional<ShoppingCart> cart = shoppingCartService.getCartByUserId(userId);
        return cart.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{cartId}/add-item")
    public ResponseEntity<?> addItemToCart(@PathVariable Long cartId, @RequestBody Map<String, Object> payload) {
        try {
            Long productId = Long.valueOf(payload.get("productId").toString());
            Integer quantity = Integer.valueOf(payload.get("quantity").toString());
    
            ShoppingCart updatedCart = shoppingCartService.addItemToCart(cartId, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{cartId}/remove-item/{itemId}")
    public ResponseEntity<ShoppingCart> removeItemFromCart(@PathVariable Long cartId, @PathVariable Long itemId) {
        ShoppingCart updatedCart = shoppingCartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping("/{cartId}/total")
    public ResponseEntity<Double> getTotalPrice(@PathVariable Long cartId) {
        double total = shoppingCartService.getTotalPrice(cartId);
        return ResponseEntity.ok(total);
    }
}
