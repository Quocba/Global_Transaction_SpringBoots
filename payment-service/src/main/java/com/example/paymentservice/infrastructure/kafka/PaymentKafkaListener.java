package com.example.paymentservice.infrastructure.kafka;

import com.example.paymentservice.application.command.PaymentCommandHandler;
import com.example.paymentservice.presentation.dto.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentKafkaListener {

    @Autowired
    private PaymentCommandHandler paymentCommandHandler;

    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void listenOrderEvents(OrderEvent event) {
        if ("ORDER_CREATED".equals(event.getType())) {
            paymentCommandHandler.handleProcessSaga(
                    event.getOrderId(),
                    event.getPrice(),
                    event.getQuantity(),
                    event.getTxId(),
                    event.getSimulatePaymentError()
            );
        }
    }
}
