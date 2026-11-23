package com.pod.back.products.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull Boolean isAvailable,
        String mockupUrl,
        String category,
        Long printfulVariantId,
        // Si ces 3 champs changent par rapport au produit existant, mockupUrl est régénéré via
        // Printful (sinon la valeur envoyée ci-dessus est prise telle quelle).
        Long printfulCatalogProductId,
        Long printfulCatalogVariantId,
        String designImageUrl,
        String mockupPlacement
) {
}
