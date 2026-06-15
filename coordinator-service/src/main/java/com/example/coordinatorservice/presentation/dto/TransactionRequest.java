package com.example.coordinatorservice.presentation.dto;

import java.math.BigDecimal;

public class TransactionRequest {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private Boolean simulatePaymentError;

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

    public Boolean getSimulatePaymentError() {
        return simulatePaymentError;
    }

    public void setSimulatePaymentError(Boolean simulatePaymentError) {
        this.simulatePaymentError = simulatePaymentError;
    }
}
