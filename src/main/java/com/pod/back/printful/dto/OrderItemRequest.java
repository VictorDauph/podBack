package com.pod.back.printful.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long syncVariantId,
        @Min(1) int quantity,
        String externalId
) {
}
