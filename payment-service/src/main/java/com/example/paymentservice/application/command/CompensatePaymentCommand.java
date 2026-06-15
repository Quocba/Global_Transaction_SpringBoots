package com.example.paymentservice.application.command;

import java.math.BigDecimal;

public class CompensatePaymentCommand {
    private Long orderId;
    private BigDecimal amount;

    public CompensatePaymentCommand(Long orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
