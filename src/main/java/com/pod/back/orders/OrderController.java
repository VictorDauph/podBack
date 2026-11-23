package com.pod.back.orders;

import com.pod.back.entities.User;
import com.pod.back.orders.dto.CreateOrderRequest;
import com.pod.back.orders.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Actions utilisateur uniquement -- voir AdminOrderController pour la gestion (admin).
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(user, request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOwn(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.listOwn(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOne(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOwnOrAdmin(user, id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancel(user, id));
    }
}
