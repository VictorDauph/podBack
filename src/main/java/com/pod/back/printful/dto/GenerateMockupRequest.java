package com.pod.back.printful.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateMockupRequest(
        @NotNull @Schema(example = "279") Long productId,
        @NotNull @Schema(example = "9063") Long variantId,
        @NotBlank @Schema(example = "https://i.ibb.co/QtszQMz/cthulhu-detoure.png") String imageUrl,
        // Optionnel : si absent, "front" est utilisé par défaut.
        @Schema(example = "front", defaultValue = "front") String placement
) {
}
