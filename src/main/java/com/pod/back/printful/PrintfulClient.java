package com.pod.back.printful;

import com.pod.back.printful.config.PrintfulJsonMapperFactory;
import com.pod.back.printful.dto.PrintfulEnvelope;
import com.pod.back.printful.exception.PrintfulApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

// Wrapper HTTP bas niveau : exécute les appels via le RestClient dédié Printful, déballe
// l'enveloppe {code, result, error} et traduit toute erreur HTTP en PrintfulApiException.
// Aucune logique métier ici : voir PrintfulService pour l'orchestration.
@Component
public class PrintfulClient {

    private final RestClient restClient;
    private final JsonMapper printfulJsonMapper = PrintfulJsonMapperFactory.create();

    public PrintfulClient(RestClient printfulRestClient) {
        this.restClient = printfulRestClient;
    }

    public <T> T get(String uri, Class<T> resultType) {
        PrintfulEnvelope<T> envelope = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(envelopeOf(resultType));
        return envelope != null ? envelope.result() : null;
    }

    public <T> List<T> getList(String uri, Class<T> elementType) {
        ParameterizedTypeReference<PrintfulEnvelope<List<T>>> type =
                ParameterizedTypeReference.forType(envelopeOfListType(elementType));
        PrintfulEnvelope<List<T>> envelope = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(type);
        return envelope != null && envelope.result() != null ? envelope.result() : List.of();
    }

    public <T> T post(String uri, Object body, Class<T> resultType) {
        PrintfulEnvelope<T> envelope = restClient.post()
                .uri(uri)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(envelopeOf(resultType));
        return envelope != null ? envelope.result() : null;
    }

    private void handleError(org.springframework.http.HttpRequest req, ClientHttpResponse response) throws java.io.IOException {
        String body;
        try (var in = response.getBody()) {
            body = new String(in.readAllBytes());
        }
        Integer printfulCode = null;
        String message = response.getStatusText();
        try {
            PrintfulEnvelope<?> envelope = printfulJsonMapper.readValue(body, PrintfulEnvelope.class);
            printfulCode = envelope.code();
            if (envelope.error() != null && envelope.error().message() != null) {
                message = envelope.error().message();
            }
        } catch (Exception ignored) {
            if (!body.isBlank()) {
                message = body;
            }
        }
        throw new PrintfulApiException(response.getStatusCode().value(), printfulCode, message);
    }

    private <T> ParameterizedTypeReference<PrintfulEnvelope<T>> envelopeOf(Class<T> resultType) {
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{resultType};
            }

            @Override
            public Type getRawType() {
                return PrintfulEnvelope.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return ParameterizedTypeReference.forType(type);
    }

    private <T> Type envelopeOfListType(Class<T> elementType) {
        Type listType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{elementType};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{listType};
            }

            @Override
            public Type getRawType() {
                return PrintfulEnvelope.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
