package com.example.ecommerce_system.service;
import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.model.Rating;
import com.example.ecommerce_system.model.User;
import com.example.ecommerce_system.repository.ProductRepository;
import com.example.ecommerce_system.repository.RatingRepository;
import com.example.ecommerce_system.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private CategoryRepository categoryRepository;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setImageUrl(productDetails.getImageUrl());
        product.setCategory(productDetails.getCategory());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContaining(name);
    }
    

    // The new rateProduct method
    public void rateProduct(Long productId, Long userId, int scoreValue, String comment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setProduct(product);
        rating.setScore(scoreValue);
        rating.setComment(comment);

        ratingRepository.save(rating); // Save the new rating to the database
    }
}
