package com.example.ecommerce_system.controller;

import com.example.ecommerce_system.model.Product;
import com.example.ecommerce_system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductWebController {

    @Autowired
    private ProductService productService;

    // Home route to display the index page
    @GetMapping("/")
    public String home() {
        return "index"; // Render the index.html
    }

    // Get all products for the product list page
    @GetMapping("/products")
    public String getAllProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product-list"; // Render the product-list.html
    }

    // Get product by ID for product detail page
    @GetMapping("/products/{id}")
    public String getProductById(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-detail"; // Render the product-detail.html
    }

    // Get the form to add a new product
    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product()); // Create an empty product object
        return "add-product"; // Render the add-product.html
    }

    // Handle the submission of the add product form
    @PostMapping("/products")
    public String addProduct(@ModelAttribute Product product) {
        productService.createProduct(product); // Save the product
        return "redirect:/products"; // Redirect to the product list
    }
}
