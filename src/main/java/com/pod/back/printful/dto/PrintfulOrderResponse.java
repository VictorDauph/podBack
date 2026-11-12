package com.pod.back.printful.dto;

import java.util.List;

public record PrintfulOrderResponse(
        long id,
        String externalId,
        String status,
        RecipientDto recipient,
        List<OrderItemRequest> items
) {
}
