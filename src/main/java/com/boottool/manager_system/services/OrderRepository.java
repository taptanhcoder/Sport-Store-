package com.boottool.manager_system.services;

import com.boottool.manager_system.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<OrderView> findAllProjectedBy(Pageable pageable);
    Optional<OrderView> findProjectedById(Long id);
}