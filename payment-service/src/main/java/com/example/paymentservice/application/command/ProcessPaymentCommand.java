package com.example.paymentservice.application.command;

import java.math.BigDecimal;

public class ProcessPaymentCommand {
    private Long orderId;
    private BigDecimal amount;
    private Boolean simulatePaymentError;

    public ProcessPaymentCommand(Long orderId, BigDecimal amount, Boolean simulatePaymentError) {
        this.orderId = orderId;
        this.amount = amount;
        this.simulatePaymentError = simulatePaymentError;
    }

    public Long getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Boolean getSimulatePaymentError() {
        return simulatePaymentError;
    }
}
