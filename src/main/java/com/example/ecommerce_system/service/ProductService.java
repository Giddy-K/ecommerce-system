package com.example.ecommerce_system.service;

import com.example.ecommerce_system.model.Category;
import com.example.ecommerce_system.model.CategoryType;
import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.repository.CategoryRepository;
import com.example.ecommerce_system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    // @Autowired
    // private CategoryRepository categoryRepository;

    public Product createProduct(Product product) {
        // Ensure category exists
        // if (product.getCategory() != null && product.getCategory().getName() != null) {
        //     CategoryType categoryName = product.getCategory().getName();
        //     Category category = categoryRepository.findByName(categoryName)
        //             .orElseGet(() -> categoryRepository.save(new Category(categoryName)));
        //     product.setCategory(category);
        // }
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
        product.setRating(productDetails.getRating());
        product.setCategory(productDetails.getCategory());

        // // Update category if provided
        // if (productDetails.getCategory() != null && productDetails.getCategory().getName() != null) {
        //     CategoryType categoryName = productDetails.getCategory().getName();
        //     Category category = categoryRepository.findByName(categoryName)
        //             .orElseGet(() -> categoryRepository.save(new Category(categoryName)));
        //     product.setCategory(category);
        // }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // public List<Product> getProductsByCategoryId(Long categoryId) {
    //     return productRepository.findByCategoryId(categoryId);
    // }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContaining(name);
    }
}
