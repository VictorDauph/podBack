package com.pod.back.printful.dto;

import tools.jackson.databind.JsonNode;

// Désérialisé par le Jackson global de l'app (camelCase), pas par printfulJsonMapper : les clés
// top-level (type/store/data) sont des mots simples, identiques dans les deux conventions.
public record PrintfulWebhookPayload(String type, Long store, JsonNode data) {
}
