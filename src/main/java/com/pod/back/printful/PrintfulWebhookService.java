package com.pod.back.printful;

import com.pod.back.printful.dto.PrintfulWebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PrintfulWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PrintfulWebhookService.class);

    public void handle(PrintfulWebhookPayload payload) {
        switch (payload.type()) {
            case "package_shipped" -> log.info("Printful: colis expédié - {}", payload.data());
            case "order_created", "order_updated", "order_failed", "order_canceled" ->
                    log.info("Printful: événement commande {} - {}", payload.type(), payload.data());
            case "product_synced", "product_updated" ->
                    log.info("Printful: événement produit {} - {}", payload.type(), payload.data());
            default -> log.warn("Printful: type de webhook non géré : {}", payload.type());
        }
        // TODO: une fois l'OrderRepository/le flux de checkout en place, répercuter ces
        // événements sur le statut de l'Order correspondant (via external_id).
    }
}
