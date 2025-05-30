package com.boottool.manager_system.services;



import com.boottool.manager_system.models.CreateOrderRequest;;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderView create(CreateOrderRequest request);
    Page<OrderView> list(Pageable pageable);
    OrderView getById(Long id);
    OrderView updateStatus(Long id, String status);
}