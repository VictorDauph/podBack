package com.pod.back.storage;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/images")
public class AdminImageUploadController {

    private final ImageStorageService imageStorageService;

    public AdminImageUploadController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    // Upload l'image de design source (préfixe "designs/" sur R2), ensuite utilisable comme
    // designImageUrl lors de la création/mise à jour d'un produit.
    @PostMapping(value = "/designs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadDesign(
            @RequestPart("file")
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary")))
            MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", uploadImage(file, "designs")));
    }

    private String uploadImage(MultipartFile file, String keyPrefix) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier envoyé est vide");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier envoyé n'est pas une image (content-type=" + contentType + ")");
        }
        try {
            return imageStorageService.upload(file.getBytes(), contentType, keyPrefix);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier envoyé", e);
        }
    }
}
