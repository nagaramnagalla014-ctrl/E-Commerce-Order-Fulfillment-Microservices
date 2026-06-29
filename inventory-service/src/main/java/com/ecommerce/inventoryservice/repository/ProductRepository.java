package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(String category);
    List<Product> findByStockQuantityLessThanEqualAndActiveTrue(int threshold);
}
