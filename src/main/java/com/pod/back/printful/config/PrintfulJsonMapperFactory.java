package com.pod.back.printful.config;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

// Volontairement pas un @Bean Spring : si un JsonMapper/ObjectMapper snake_case est le seul de
// son type dans le contexte, Spring Boot l'adopte comme convertisseur JSON global de l'app (vu
// en pratique : casse le camelCase attendu par le reste de l'API). On construit donc une
// instance à la demande, utilisée uniquement par PrintfulClientConfig et PrintfulClient.
public final class PrintfulJsonMapperFactory {

    private PrintfulJsonMapperFactory() {
    }

    public static JsonMapper create() {
        return JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }
}
