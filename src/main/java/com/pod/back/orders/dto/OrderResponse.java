package com.pod.back.orders.dto;

import com.pod.back.entities.Address;
import com.pod.back.entities.Order;
import com.pod.back.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus orderStatus,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        Address shippingAddress,
        Long printfulOrderId,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getShippingAddress(),
                order.getPrintfulOrderId(),
                order.getCreatedAt()
        );
    }
}
