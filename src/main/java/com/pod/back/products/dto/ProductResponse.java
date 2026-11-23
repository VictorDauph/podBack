package com.pod.back.products.dto;

import com.pod.back.entities.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Boolean isAvailable,
        String mockupUrl,
        String category,
        Long printfulVariantId,
        Long printfulCatalogProductId,
        Long printfulCatalogVariantId,
        String designImageUrl,
        String mockupPlacement
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getIsAvailable(),
                product.getMockupUrl(),
                product.getCategory(),
                product.getPrintfulVariantId(),
                product.getPrintfulCatalogProductId(),
                product.getPrintfulCatalogVariantId(),
                product.getDesignImageUrl(),
                product.getMockupPlacement()
        );
    }
}
