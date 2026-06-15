package com.example.orderservice.infrastructure.persistence;

import com.example.orderservice.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByTxId(String txId);
}
