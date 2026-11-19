package com.pod.back.repositories;

import com.pod.back.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsAvailableTrue();
}
