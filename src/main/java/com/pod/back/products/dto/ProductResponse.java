package com.pod.back.products.dto;

import com.pod.back.entities.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Boolean isAvailable,
        String imageUrl,
        String category,
        Long printfulVariantId
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getIsAvailable(),
                product.getImageUrl(),
                product.getCategory(),
                product.getPrintfulVariantId()
        );
    }
}
