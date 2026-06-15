package com.example.coordinatorservice.application.service;

import com.example.coordinatorservice.presentation.dto.TransactionRequest;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransactionOrchestrator {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.services.order-url:http://localhost:8081}")
    private String orderServiceUrl;

    @Value("${app.services.payment-url:http://localhost:8082}")
    private String paymentServiceUrl;

    @GlobalTransactional(name = "coordinator-global-at", rollbackFor = Exception.class)
    public Map<String, Object> executeGlobalTransactionAt(TransactionRequest request) {
        String orderUrl = orderServiceUrl + "/api/orders/pure-at";
        Map<String, Object> orderReq = new HashMap<>();
        orderReq.put("productId", request.getProductId());
        orderReq.put("quantity", request.getQuantity());
        orderReq.put("price", request.getPrice());
        orderReq.put("simulatePaymentError", request.getSimulatePaymentError());

        Map<String, Object> orderResponse;
        try {
            orderResponse = restTemplate.postForObject(orderUrl, orderReq, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Order creation failed in AT mode", e);
        }

        if (orderResponse == null || orderResponse.get("data") == null) {
            throw new RuntimeException("Invalid response from Order Service");
        }

        Map<String, Object> orderData = (Map<String, Object>) orderResponse.get("data");
        Number orderId = (Number) orderData.get("id");

        String paymentUrl = paymentServiceUrl + "/api/payments/at";
        Map<String, Object> paymentReq = new HashMap<>();
        paymentReq.put("orderId", orderId.longValue());
        paymentReq.put("amount", request.getPrice().multiply(new BigDecimal(request.getQuantity())));
        paymentReq.put("simulatePaymentError", request.getSimulatePaymentError());

        try {
            restTemplate.postForEntity(paymentUrl, paymentReq, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Payment processing failed in AT mode. Global transaction will rollback.", e);
        }

        return orderData;
    }

    public Map<String, Object> executeSagaOrchestration(TransactionRequest request) {
        String orderUrl = orderServiceUrl + "/api/orders/pure-saga";
        Map<String, Object> orderReq = new HashMap<>();
        orderReq.put("productId", request.getProductId());
        orderReq.put("quantity", request.getQuantity());
        orderReq.put("price", request.getPrice());
        orderReq.put("simulatePaymentError", request.getSimulatePaymentError());

        Map<String, Object> orderResponse;
        try {
            orderResponse = restTemplate.postForObject(orderUrl, orderReq, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Saga Order creation failed", e);
        }

        if (orderResponse == null || orderResponse.get("data") == null) {
            throw new RuntimeException("Invalid response from Order Service in Saga mode");
        }

        Map<String, Object> orderData = (Map<String, Object>) orderResponse.get("data");
        Number orderId = (Number) orderData.get("id");

        String paymentUrl = paymentServiceUrl + "/api/payments/saga/process";
        Map<String, Object> paymentReq = new HashMap<>();
        paymentReq.put("orderId", orderId.longValue());
        paymentReq.put("amount", request.getPrice().multiply(new BigDecimal(request.getQuantity())));
        paymentReq.put("simulatePaymentError", request.getSimulatePaymentError());

        try {
            restTemplate.postForEntity(paymentUrl, paymentReq, Map.class);

            String approveUrl = orderServiceUrl + "/api/orders/" + orderId.longValue() + "/status?status=APPROVED";
            restTemplate.put(approveUrl, null);
            orderData.put("status", "APPROVED");
        } catch (Exception e) {
            try {
                String cancelUrl = orderServiceUrl + "/api/orders/" + orderId.longValue() + "/status?status=CANCELLED";
                restTemplate.put(cancelUrl, null);
            } catch (Exception ex) {
            }

            try {
                String compensatePaymentUrl = paymentServiceUrl + "/api/payments/saga/compensate";
                restTemplate.postForEntity(compensatePaymentUrl, paymentReq, Map.class);
            } catch (Exception ex) {
            }

            throw new RuntimeException("Saga Transaction failed. Compensating transactions executed.", e);
        }

        return orderData;
    }

    @GlobalTransactional(name = "coordinator-global-tcc", rollbackFor = Exception.class)
    public Map<String, Object> executeGlobalTransactionTcc(TransactionRequest request) {
        String orderUrl = orderServiceUrl + "/api/orders/tcc-prepare";
        Map<String, Object> orderReq = new HashMap<>();
        orderReq.put("productId", request.getProductId());
        orderReq.put("quantity", request.getQuantity());
        orderReq.put("price", request.getPrice());
        orderReq.put("simulatePaymentError", request.getSimulatePaymentError());

        Map<String, Object> orderResponse;
        try {
            orderResponse = restTemplate.postForObject(orderUrl, orderReq, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Order TCC prepare failed", e);
        }

        if (orderResponse == null || orderResponse.get("data") == null) {
            throw new RuntimeException("Invalid TCC response from Order Service");
        }

        Number orderId = (Number) orderResponse.get("data");

        String paymentUrl = paymentServiceUrl + "/api/payments/tcc/prepare";
        Map<String, Object> paymentReq = new HashMap<>();
        paymentReq.put("orderId", orderId.longValue());
        paymentReq.put("amount", request.getPrice().multiply(new BigDecimal(request.getQuantity())));
        paymentReq.put("simulatePaymentError", request.getSimulatePaymentError());

        try {
            restTemplate.postForEntity(paymentUrl, paymentReq, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Payment TCC prepare failed. Global transaction will rollback.", e);
        }

        Map<String, Object> mockOrderData = new HashMap<>();
        mockOrderData.put("id", orderId.longValue());
        mockOrderData.put("productId", request.getProductId());
        mockOrderData.put("quantity", request.getQuantity());
        mockOrderData.put("price", request.getPrice());
        mockOrderData.put("status", "APPROVED");
        return mockOrderData;
    }

    public Map<String, Object> executeSagaChoreography(TransactionRequest request) {
        String orderUrl = orderServiceUrl + "/api/orders/saga";
        Map<String, Object> orderReq = new HashMap<>();
        orderReq.put("productId", request.getProductId());
        orderReq.put("quantity", request.getQuantity());
        orderReq.put("price", request.getPrice());
        orderReq.put("simulatePaymentError", request.getSimulatePaymentError());

        Map<String, Object> response;
        try {
            response = restTemplate.postForObject(orderUrl, orderReq, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Saga Choreography start failed", e);
        }

        if (response == null || response.get("data") == null) {
            throw new RuntimeException("Invalid response from Order Service in Choreography mode");
        }

        return (Map<String, Object>) response.get("data");
    }

    public void cleanAllDatabases() {
        try {
            restTemplate.delete(orderServiceUrl + "/api/orders/clean-all");
        } catch (Exception e) {
            System.err.println("Failed to clean orders: " + e.getMessage());
        }

        try {
            restTemplate.delete(paymentServiceUrl + "/api/payments/clean-all");
        } catch (Exception e) {
            System.err.println("Failed to clean payments: " + e.getMessage());
        }
    }
}



