package com.example.orderservice.domain.repository;

import com.example.orderservice.domain.model.Order;
import java.util.Optional;
import java.util.List;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    Optional<Order> findByTxId(String txId);
    List<Order> findAll();
}
