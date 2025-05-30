package com.boottool.manager_system.services.impl;

import com.boottool.manager_system.models.*;
import com.boottool.manager_system.services.OrderRepository;
import com.boottool.manager_system.services.ProductsRepository;
import com.boottool.manager_system.services.OrderService;
import com.boottool.manager_system.services.OrderView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final ProductsRepository productRepo;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepo,
                            ProductsRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    @Override
    public OrderView create(CreateOrderRequest req) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        order.setItems(
                req.getItems().stream().map(i -> {
                    Long prodId = i.getProductId();

                    Product p = productRepo.findById(prodId)
                            .orElseThrow(() -> new ApiException("Product not found: " + prodId));

                    if (p.getStockQuantity() < i.getQuantity()) {
                        throw new ApiException("Insufficient stock: " + p.getName());
                    }
                    p.setStockQuantity(p.getStockQuantity() - i.getQuantity());

                    OrderItem oi = new OrderItem();
                    oi.setOrder(order);
                    oi.setProduct(p);
                    oi.setQuantity(i.getQuantity());
                    oi.setUnitPrice(p.getPrice());
                    return oi;
                }).collect(Collectors.toList())
        );

        Order saved = orderRepo.save(order);

        return orderRepo.findProjectedById(saved.getId())
                .orElseThrow(() -> new ApiException("Error loading created order"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderView> list(Pageable pageable) {
        return orderRepo.findAllProjectedBy(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderView getById(Long id) {
        return orderRepo.findProjectedById(id)
                .orElseThrow(() -> new ApiException("Order not found: " + id));
    }

    @Override
    public OrderView updateStatus(Long id, String status) {
        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new ApiException("Order not found: " + id));
        o.setStatus(OrderStatus.valueOf(status));
        return getById(id);
    }
}