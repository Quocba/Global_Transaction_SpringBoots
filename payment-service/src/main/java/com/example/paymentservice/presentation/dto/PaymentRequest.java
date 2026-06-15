package com.example.paymentservice.presentation.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private Boolean simulatePaymentError;

    public PaymentRequest() {
    }

    public PaymentRequest(Long orderId, BigDecimal amount, Boolean simulatePaymentError) {
        this.orderId = orderId;
        this.amount = amount;
        this.simulatePaymentError = simulatePaymentError;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getSimulatePaymentError() {
        return simulatePaymentError;
    }

    public void setSimulatePaymentError(Boolean simulatePaymentError) {
        this.simulatePaymentError = simulatePaymentError;
    }
}
