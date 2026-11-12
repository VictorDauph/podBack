package com.pod.back.printful.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull @Valid RecipientDto recipient,
        @NotEmpty @Valid List<OrderItemRequest> items,
        String externalId,
        // false (défaut) = commande brouillon côté Printful, à confirmer manuellement avant
        // fabrication/expédition. Comportement le plus sûr sans clé sandbox pour tester.
        boolean confirm
) {
}
