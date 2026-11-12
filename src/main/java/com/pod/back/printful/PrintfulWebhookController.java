package com.pod.back.printful;

import com.pod.back.printful.dto.PrintfulWebhookPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/printful")
public class PrintfulWebhookController {

    private final PrintfulWebhookService webhookService;

    @Value("${printful.webhook.path-secret}")
    private String expectedSecret;

    public PrintfulWebhookController(PrintfulWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    // Printful v1 ne signe pas ses webhooks (pas de HMAC) : le secret dans le chemin, à
    // enregistrer tel quel comme URL de callback dans le dashboard Printful, fait office de
    // protection minimale. 404 (pas 401/403) en cas de mismatch pour ne pas révéler l'existence
    // de l'endpoint.
    @PostMapping("/{secret}")
    public ResponseEntity<Void> receiveWebhook(@PathVariable String secret, @RequestBody PrintfulWebhookPayload payload) {
        if (expectedSecret == null || expectedSecret.isBlank() || !expectedSecret.equals(secret)) {
            return ResponseEntity.notFound().build();
        }
        webhookService.handle(payload);
        return ResponseEntity.ok().build();
    }
}
