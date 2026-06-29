package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.model.Product;
import com.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired private InventoryService inventoryService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }

    @GetMapping("/products/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getProductBySku(sku));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createProduct(product));
    }

    @PutMapping("/products/{sku}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable String sku, @RequestBody Map<String, Integer> body) {
        int delta = body.get("delta");
        return ResponseEntity.ok(inventoryService.updateStock(sku, delta));
    }

    @GetMapping("/products/low-stock")
    public ResponseEntity<List<Product>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStock());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "inventory-service"));
    }
}
