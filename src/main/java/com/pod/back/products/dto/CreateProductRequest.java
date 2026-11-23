package com.pod.back.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank @Schema(example = "sac à dos") String name,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotNull Boolean isAvailable,
        String mockupUrl,
        @Schema(example = "accessoires")String category,
        Long printfulVariantId,
        // Les 3 champs suivants sont optionnels : si tous renseignés, un mockup est généré
        // automatiquement via Printful à la création du produit.
        @Schema(example = "279")Long printfulCatalogProductId,
        @Schema(example = "9063")Long printfulCatalogVariantId,
        @Schema(example = "https://i.ibb.co/QtszQMz/cthulhu-detoure.png") String designImageUrl,
        @Schema(example = "front") String mockupPlacement
) {
}
