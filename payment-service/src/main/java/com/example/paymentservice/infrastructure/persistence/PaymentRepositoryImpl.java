package com.example.paymentservice.infrastructure.persistence;

import com.example.paymentservice.domain.model.Payment;
import com.example.paymentservice.domain.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.List;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaPaymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaPaymentRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByTxId(String txId) {
        return jpaPaymentRepository.findByTxId(txId);
    }

    @Override
    public List<Payment> findAll() {
        return jpaPaymentRepository.findAll();
    }
}
