package com.example.paymentservice.domain.repository;

import com.example.paymentservice.domain.model.Payment;
import java.util.Optional;
import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByTxId(String txId);
    List<Payment> findAll();
}
