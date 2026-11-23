package com.pod.back.storage;

import com.pod.back.storage.exception.ImageStorageException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

// Héberge les images produit (design source + mockups générés) sur Cloudflare R2, organisées par
// préfixe de clé ("designs/", "mockups/") en guise d'équivalent d'albums -- R2 n'a pas de vraie
// notion d'album, mais un préfixe de clé S3 joue exactement ce rôle de regroupement.
@Service
public class ImageStorageService {

    private final S3Client r2Client;
    private final R2Properties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ImageStorageService(S3Client r2Client, R2Properties properties) {
        this.r2Client = r2Client;
        this.properties = properties;
    }

    public String upload(byte[] content, String contentType, String keyPrefix) {
        String key = keyPrefix + "/" + UUID.randomUUID() + extensionFor(contentType);
        try {
            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(properties.bucket())
                            .key(key)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build(),
                    RequestBody.fromBytes(content)
            );
        } catch (S3Exception e) {
            throw new ImageStorageException("Échec de l'upload vers R2 (clé=" + key + ")", e);
        }
        return properties.publicBaseUrl() + "/" + key;
    }

    // Télécharge l'image depuis une URL source (ex: mockup temporaire Printful, qui expire sous
    // 24-72h) et la réhéberge sur R2 pour obtenir une URL stable et durable.
    public String reupload(String sourceUrl, String keyPrefix) {
        HttpResponse<byte[]> response;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(sourceUrl)).GET().build();
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new ImageStorageException("Impossible de récupérer l'image source : " + sourceUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImageStorageException("Interrompu en récupérant l'image source : " + sourceUrl, e);
        }

        if (response.statusCode() >= 400) {
            throw new ImageStorageException(
                    "Téléchargement de l'image source a échoué (HTTP " + response.statusCode() + ") : " + sourceUrl,
                    null);
        }

        String contentType = response.headers().firstValue("Content-Type").orElse("application/octet-stream");
        return upload(response.body(), contentType, keyPrefix);
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            return "";
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
