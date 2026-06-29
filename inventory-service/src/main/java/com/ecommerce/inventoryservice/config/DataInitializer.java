package com.ecommerce.inventoryservice.config;

import com.ecommerce.inventoryservice.model.Product;
import com.ecommerce.inventoryservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired private ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        createProduct("LAPTOP-001", "Gaming Laptop Pro 15", "Electronics",
                "High-performance gaming laptop with RTX 4070", new BigDecimal("1299.99"), 45, 10);
        createProduct("PHONE-001", "Smartphone Ultra X", "Electronics",
                "Latest flagship smartphone with 200MP camera", new BigDecimal("899.99"), 120, 20);
        createProduct("HEADPHONE-001", "Wireless Noise-Cancelling Headphones", "Electronics",
                "Premium ANC headphones with 30hr battery", new BigDecimal("249.99"), 80, 15);
        createProduct("TABLET-001", "Professional Tablet 12\"", "Electronics",
                "High-resolution tablet for creators", new BigDecimal("649.99"), 35, 10);
        createProduct("WATCH-001", "Smart Watch Series 8", "Electronics",
                "Advanced smartwatch with health tracking", new BigDecimal("399.99"), 60, 10);
        createProduct("SHIRT-001", "Premium Cotton T-Shirt", "Clothing",
                "Soft breathable 100% cotton t-shirt", new BigDecimal("29.99"), 300, 50);
        createProduct("JEANS-001", "Slim Fit Denim Jeans", "Clothing",
                "Classic slim fit jeans in multiple colors", new BigDecimal("59.99"), 200, 40);
        createProduct("SNEAKER-001", "Running Sneakers Pro", "Footwear",
                "Lightweight running shoes with cushion support", new BigDecimal("89.99"), 150, 30);
        createProduct("BOOK-001", "Java Microservices in Practice", "Books",
                "Comprehensive guide to building microservices", new BigDecimal("49.99"), 500, 100);
        createProduct("BACKPACK-001", "Laptop Backpack 30L", "Accessories",
                "Water-resistant laptop backpack with USB port", new BigDecimal("79.99"), 100, 20);

        log.info("Seeded {} products", productRepository.count());
    }

    private void createProduct(String sku, String name, String category,
                                String description, BigDecimal price, int stock, int reorder) {
        Product p = new Product();
        p.setSku(sku);
        p.setName(name);
        p.setCategory(category);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQuantity(stock);
        p.setReorderLevel(reorder);
        productRepository.save(p);
    }
}
