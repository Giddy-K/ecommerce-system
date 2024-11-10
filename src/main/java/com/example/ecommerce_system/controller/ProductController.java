package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.model.Rating;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.service.ProductService;
import com.example.ecommerce_system.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // CREATE A PRODUCT [ADMIN] [TODO] : AUTH
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // GET A SPECIFIC PRODUCT WITH ITS DETAILS [ANY]
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // GET ALL THE PRODUCTS [ANY]
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // UPDATE A PRODUCT [ADMIN] [TODO]: AUTH
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    // DELETE A PRODUCT [ADMIN] [TODO]: AUTH
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // SEARCH FOR A PRODUCT [ANY] [TODO]
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProducts(name);
        return ResponseEntity.ok(products);
    }

    // RATE A PRODUCT
    @PostMapping("/rate-product")
    public ResponseEntity<?> rateProduct(@RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String token) {
        Optional<User> user = userService.getUserFromToken(token);
        if (user.isPresent()) {
            Long productId = Long.parseLong(request.get("id").toString());
            int score = Integer.parseInt(request.get("score").toString());
            String comment = request.get("comment").toString();
            productService.rateProduct(productId, user.get().getId(), score, comment);
            return ResponseEntity.ok("Product rated successfully.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
    }

    // GET RATINGS FOR A SPECIFIC PRODUCT [ANY]
    @GetMapping("/{id}/ratings")
    public ResponseEntity<?> getProductRatings(@PathVariable Long id) {
        Optional<Product> productOpt = productService.getProductById(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            List<Rating> ratings = product.getRatings();

            // Calculate the overall rating
            double averageRating = ratings.stream()
                    .mapToInt(Rating::getScore)
                    .average()
                    .orElse(0.0);

            // Create a response with ratings and average rating
            Map<String, Object> response = Map.of(
                    "averageRating", averageRating,
                    "ratings", ratings);

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
    }

}
