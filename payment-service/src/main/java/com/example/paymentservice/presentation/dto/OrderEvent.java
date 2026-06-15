package com.example.paymentservice.presentation.dto;

import java.math.BigDecimal;

public class OrderEvent {
    private Long orderId;
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private String txId;
    private String type;
    private Boolean simulatePaymentError;

    public OrderEvent() {
    }

    public OrderEvent(Long orderId, String productId, Integer quantity, BigDecimal price, String txId, String type, Boolean simulatePaymentError) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.txId = txId;
        this.type = type;
        this.simulatePaymentError = simulatePaymentError;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSimulatePaymentError() {
        return simulatePaymentError;
    }

    public void setSimulatePaymentError(Boolean simulatePaymentError) {
        this.simulatePaymentError = simulatePaymentError;
    }
}
