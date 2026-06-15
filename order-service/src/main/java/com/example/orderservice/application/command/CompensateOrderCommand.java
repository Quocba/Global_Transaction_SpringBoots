package com.example.orderservice.application.command;

public class CompensateOrderCommand {
    private String txId;

    public CompensateOrderCommand(String txId) {
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }
}
