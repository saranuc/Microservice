package com.microservice.product_service.Controller;

import com.microservice.product_service.Entity.Product;
import com.microservice.product_service.Repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@EnableCaching
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
    @Cacheable(value = "product", key = "'allProducts'")
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }


    @GetMapping("/{productId}")
    @Cacheable(value = "product", key = "#productId")
    public Product getProductById(@PathVariable long productId) {
        return productRepo.findById(productId).orElseThrow(() -> new RuntimeException("product not found!"));

    }
}
