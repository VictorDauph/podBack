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
        String imageUrl,
        String category,
        Long printfulVariantId
) {
}
