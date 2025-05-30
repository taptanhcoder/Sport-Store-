package com.boottool.manager_system.services;

import com.boottool.manager_system.models.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderView {
    Long getId();
    LocalDateTime getCreatedAt();
    OrderStatus getStatus();
    List<OrderItemView> getItems();

    default BigDecimal getTotalAmount() {
        return getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    interface OrderItemView {
        Long getProductId();
        String getProductName();
        int getQuantity();
        BigDecimal getUnitPrice();
    }
}