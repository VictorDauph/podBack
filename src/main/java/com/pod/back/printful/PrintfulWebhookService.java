package com.pod.back.printful;

import com.pod.back.entities.Order;
import com.pod.back.enums.OrderStatus;
import com.pod.back.printful.dto.PrintfulWebhookPayload;
import com.pod.back.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

@Service
public class PrintfulWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PrintfulWebhookService.class);

    private final OrderRepository orderRepository;

    public PrintfulWebhookService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void handle(PrintfulWebhookPayload payload) {
        switch (payload.type()) {
            case "package_shipped" -> updateOrderStatus(payload, OrderStatus.SHIPPED);
            case "order_created", "order_updated" -> updateOrderStatus(payload, OrderStatus.PROCESSING);
            case "order_failed", "order_canceled" -> updateOrderStatus(payload, OrderStatus.CANCELLED);
            case "product_synced", "product_updated" ->
                    log.info("Printful: événement produit {} - {}", payload.type(), payload.data());
            default -> log.warn("Printful: type de webhook non géré : {}", payload.type());
        }
    }

    // Ne doit jamais lever d'exception pour un cas "attendu" (external_id absent/invalide,
    // commande locale introuvable) : un 500 ferait juste relancer le webhook par Printful en
    // boucle pour un cas qui ne se résoudra jamais tout seul. Les erreurs inattendues (ex: BDD
    // injoignable) remontent normalement et déclenchent un 500 -- ça a du sens de laisser
    // Printful réessayer dans ce cas-là.
    private void updateOrderStatus(PrintfulWebhookPayload payload, OrderStatus newStatus) {
        Long orderId = extractOrderId(payload);
        if (orderId == null) {
            log.warn("Printful webhook {} ignoré : external_id absent/invalide dans data={}",
                    payload.type(), payload.data());
            return;
        }

        Optional<Order> maybeOrder = orderRepository.findById(orderId);
        if (maybeOrder.isEmpty()) {
            log.warn("Printful webhook {} ignoré : aucune commande locale avec id={}", payload.type(), orderId);
            return;
        }

        Order order = maybeOrder.get();
        log.info("Printful webhook {} : commande {} {} -> {}", payload.type(), orderId, order.getOrderStatus(), newStatus);
        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }

    // external_id a été fixé à la création de la commande Printful (PrintfulOrderRequestBody)
    // comme étant l'id local de l'Order (String). D'après la doc Printful, l'objet commande est
    // imbriqué sous data.order pour les événements order_* et package_shipped ; on retombe sur
    // data.external_id au cas où un événement place le champ au premier niveau. À confirmer avec
    // un vrai webhook de test avant mise en prod.
    private Long extractOrderId(PrintfulWebhookPayload payload) {
        JsonNode data = payload.data();
        if (data == null) {
            return null;
        }

        JsonNode orderNode = data.get("order");
        JsonNode externalIdNode = orderNode != null ? orderNode.get("external_id") : data.get("external_id");
        if (externalIdNode == null || externalIdNode.isNull()) {
            return null;
        }

        String externalId = externalIdNode.asString(null);
        if (externalId == null || externalId.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(externalId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
