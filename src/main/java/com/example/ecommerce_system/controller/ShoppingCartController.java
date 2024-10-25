package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.model.ShoppingCart;
import com.example.ecommerce_system.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping
    public List<ShoppingCart> getAllCarts() {
        return shoppingCartService.getAllCarts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShoppingCart> getCartById(@PathVariable Long id) {
        ShoppingCart cart = shoppingCartService.getCartById(id);
        return ResponseEntity.ok(cart);
    }

    // Add the product to the cart
    @PostMapping("/add/{id}")
    public ResponseEntity<ShoppingCart> addToCart(@PathVariable Long id) {
        ShoppingCart updatedCart = shoppingCartService.addToCart(id);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        shoppingCartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }
}
