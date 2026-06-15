package com.example.orderservice.infrastructure.kafka;

import com.example.orderservice.application.command.CompensateOrderCommand;
import com.example.orderservice.application.command.OrderCommandHandler;
import com.example.orderservice.presentation.dto.PaymentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaListener {

    @Autowired
    private OrderCommandHandler orderCommandHandler;

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void listenPaymentEvents(PaymentEvent event) {
        if ("PAYMENT_FAILED".equals(event.getStatus())) {
            orderCommandHandler.handleCompensate(new CompensateOrderCommand(event.getTxId()));
        }
    }
}
