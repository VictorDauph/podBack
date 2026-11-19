package com.pod.back.orders;

import com.pod.back.entities.Address;
import com.pod.back.entities.Order;
import com.pod.back.entities.OrderItem;
import com.pod.back.entities.Product;
import com.pod.back.entities.User;
import com.pod.back.enums.OrderStatus;
import com.pod.back.exceptions.ResourceNotFoundException;
import com.pod.back.orders.dto.CreateOrderRequest;
import com.pod.back.orders.dto.OrderItemRequest;
import com.pod.back.orders.dto.OrderResponse;
import com.pod.back.printful.PrintfulService;
import com.pod.back.printful.dto.PrintfulOrderResponse;
import com.pod.back.printful.dto.RecipientDto;
import com.pod.back.repositories.OrderRepository;
import com.pod.back.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PrintfulService printfulService;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                         PrintfulService printfulService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.printfulService = printfulService;
    }

    @Transactional
    public OrderResponse create(User user, CreateOrderRequest request) {
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produit introuvable (id=" + itemRequest.productId() + ")"));
            if (!Boolean.TRUE.equals(product.getIsAvailable())) {
                throw new IllegalArgumentException("Produit indisponible : " + product.getName());
            }

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));

            items.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .price(product.getPrice())
                    .variant(itemRequest.variant())
                    .build());
        }

        Order order = Order.builder()
                .user(user)
                .items(items)
                .totalAmount(total)
                .orderStatus(OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress().toEntity())
                .build();
        items.forEach(item -> item.setOrder(order));

        return OrderResponse.from(orderRepository.save(order));
    }

    public List<OrderResponse> listOwn(User user) {
        return orderRepository.findByUser(user).stream()
                .map(OrderResponse::from)
                .toList();
    }

    public List<OrderResponse> listAll() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse getOwnOrAdmin(User user, Long orderId) {
        Order order = findOrThrow(orderId);
        requireOwnerOrAdmin(user, order);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(User user, Long orderId) {
        Order order = findOrThrow(orderId);
        requireOwnerOrAdmin(user, order);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Seule une commande PENDING peut être annulée (statut actuel : " + order.getOrderStatus() + ")");
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(orderRepository.save(order));
    }

    // Déclenché par une action admin faute d'intégration de paiement pour l'instant : marque la
    // commande payée et la pousse aussitôt chez Printful en brouillon (confirm=false). Une seule
    // transaction : si l'appel Printful échoue, le passage à PAID est annulé avec le reste
    // (rollback), la commande redevient PENDING et l'admin peut retenter /pay.
    @Transactional
    public OrderResponse markPaidAndPushToPrintful(Long orderId) {
        Order order = findOrThrow(orderId);
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Seule une commande PENDING peut être marquée payée (statut actuel : " + order.getOrderStatus() + ")");
        }

        List<Long> missingMapping = order.getItems().stream()
                .filter(item -> item.getProduct().getPrintfulVariantId() == null)
                .map(item -> item.getProduct().getId())
                .toList();
        if (!missingMapping.isEmpty()) {
            throw new IllegalStateException(
                    "Produits sans mapping Printful (printfulVariantId manquant) : " + missingMapping);
        }

        order.setOrderStatus(OrderStatus.PAID);

        RecipientDto recipient = buildRecipient(order);
        List<com.pod.back.printful.dto.OrderItemRequest> printfulItems = order.getItems().stream()
                .map(item -> new com.pod.back.printful.dto.OrderItemRequest(
                        item.getProduct().getPrintfulVariantId(), item.getQuantity(), null))
                .toList();

        var printfulRequest = new com.pod.back.printful.dto.CreateOrderRequest(
                recipient, printfulItems, String.valueOf(order.getId()), false);

        PrintfulOrderResponse printfulResponse = printfulService.createOrder(printfulRequest);
        order.setPrintfulOrderId(printfulResponse.id());

        return OrderResponse.from(orderRepository.save(order));
    }

    private RecipientDto buildRecipient(Order order) {
        Address address = order.getShippingAddress();
        User user = order.getUser();
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return new RecipientDto(
                fullName.isBlank() ? user.getEmail() : fullName,
                address.getStreet(),
                address.getComplement(),
                address.getCity(),
                null,
                address.getCountryCode(),
                address.getZipCode(),
                null,
                user.getEmail()
        );
    }

    private Order findOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable (id=" + orderId + ")"));
    }

    // 404 (pas 403) pour un appelant qui n'est ni propriétaire ni admin : ne révèle pas
    // l'existence de la commande d'un autre utilisateur, même convention que le secret du
    // webhook Printful.
    private void requireOwnerOrAdmin(User user, Order order) {
        boolean isOwner = order.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == com.pod.back.enums.Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResourceNotFoundException("Commande introuvable (id=" + order.getId() + ")");
        }
    }
}
