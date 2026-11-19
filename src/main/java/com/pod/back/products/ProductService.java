package com.pod.back.products;

import com.pod.back.entities.Product;
import com.pod.back.exceptions.ResourceNotFoundException;
import com.pod.back.products.dto.CreateProductRequest;
import com.pod.back.products.dto.ProductResponse;
import com.pod.back.products.dto.UpdateProductRequest;
import com.pod.back.repositories.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> listAvailable() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    public List<ProductResponse> listAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse create(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .isAvailable(request.isAvailable())
                .imageUrl(request.imageUrl())
                .category(request.category())
                .printfulVariantId(request.printfulVariantId())
                .build();
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findOrThrow(id);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setIsAvailable(request.isAvailable());
        product.setImageUrl(request.imageUrl());
        product.setCategory(request.category());
        product.setPrintfulVariantId(request.printfulVariantId());
        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = findOrThrow(id);
        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Impossible de supprimer un produit référencé par des commandes existantes");
        }
    }

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable (id=" + id + ")"));
    }
}
