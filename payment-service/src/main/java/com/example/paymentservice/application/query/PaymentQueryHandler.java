package com.example.paymentservice.application.query;

import com.example.paymentservice.domain.model.Payment;
import com.example.paymentservice.domain.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentQueryHandler {

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Payment> handle(GetAllPaymentsQuery query) {
        return paymentRepository.findAll();
    }
}
