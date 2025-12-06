package com.microservice.product_service.Controller;

import com.microservice.product_service.Entity.Product;
import com.microservice.product_service.Repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepo productRepo;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productRepo.save(product);
    }
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        return productRepo.findById(productId).orElseThrow(() -> new RuntimeException("product not found!"));

    }
}
