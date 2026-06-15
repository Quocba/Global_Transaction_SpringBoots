package com.example.orderservice.presentation.dto;

import java.math.BigDecimal;

public class OrderRequest {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private Boolean simulatePaymentError;

    public OrderRequest() {
    }

    public OrderRequest(String productId, Integer quantity, BigDecimal price, Boolean simulatePaymentError) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.simulatePaymentError = simulatePaymentError;
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

    public Boolean getSimulatePaymentError() {
        return simulatePaymentError;
    }

    public void setSimulatePaymentError(Boolean simulatePaymentError) {
        this.simulatePaymentError = simulatePaymentError;
    }
}
