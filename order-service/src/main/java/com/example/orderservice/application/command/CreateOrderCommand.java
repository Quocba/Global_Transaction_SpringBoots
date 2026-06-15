package com.example.orderservice.application.command;

import java.math.BigDecimal;

public class CreateOrderCommand {
    private String productId;
    private Integer quantity;
    private BigDecimal price;
    private Boolean simulatePaymentError;

    public CreateOrderCommand(String productId, Integer quantity, BigDecimal price, Boolean simulatePaymentError) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.simulatePaymentError = simulatePaymentError;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Boolean getSimulatePaymentError() {
        return simulatePaymentError;
    }
}
