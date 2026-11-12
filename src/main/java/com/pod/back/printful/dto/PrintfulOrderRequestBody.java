package com.pod.back.printful.dto;

import java.util.List;

// Corps envoyé à POST /orders — `confirm` est un paramètre de requête (?confirm=1), pas un
// champ du corps, donc on ne le reprend pas ici (voir PrintfulService.createOrder).
public record PrintfulOrderRequestBody(
        RecipientDto recipient,
        List<OrderItemRequest> items,
        String externalId
) {
}
