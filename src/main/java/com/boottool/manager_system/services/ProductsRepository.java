package com.boottool.manager_system.services;

import com.boottool.manager_system.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Product, Integer> {
}
