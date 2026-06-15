package com.example.paymentservice.presentation.dto;

public class PaymentEvent {
    private Long orderId;
    private String txId;
    private String status;

    public PaymentEvent() {
    }

    public PaymentEvent(Long orderId, String txId, String status) {
        this.orderId = orderId;
        this.txId = txId;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
