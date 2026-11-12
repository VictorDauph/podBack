package com.pod.back.printful.dto;

// Toutes les réponses de l'API Printful sont enveloppées sous la forme { code, result, error }
public record PrintfulEnvelope<T>(int code, T result, PrintfulError error) {
}
