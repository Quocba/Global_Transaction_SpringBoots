package com.example.paymentservice.infrastructure.persistence;

import com.example.paymentservice.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTxId(String txId);
}
