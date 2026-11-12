package com.pod.back.printful.dto;

// Printful renvoie les montants sous forme de chaînes JSON (ex: "retail_price": "25.00"),
// pas de nombres : on garde String ici et on convertit en BigDecimal uniquement si besoin.
public record SyncVariant(
        long id,
        String externalId,
        long syncProductId,
        String name,
        String retailPrice,
        String currency,
        String sku
) {
}
