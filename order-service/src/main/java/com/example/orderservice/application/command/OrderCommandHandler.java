package com.example.orderservice.application.command;

import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;
import com.example.orderservice.infrastructure.tcc.OrderTccAction;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderCommandHandler {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderTccAction orderTccAction;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.services.payment-url:http://localhost:8082}")
    private String paymentServiceUrl;

    @GlobalTransactional(name = "create-order-tcc", rollbackFor = Exception.class)
    public Order handleCreateTcc(CreateOrderCommand command) {
        Long orderId = orderTccAction.prepare(null, command.getProductId(), command.getQuantity(), command.getPrice());

        String paymentUrl = paymentServiceUrl + "/api/payments/tcc/prepare";
        Map<String, Object> paymentReq = new HashMap<>();
        paymentReq.put("orderId", orderId);
        paymentReq.put("amount", command.getPrice().multiply(new BigDecimal(command.getQuantity())));
        paymentReq.put("simulatePaymentError", command.getSimulatePaymentError());

        try {
            restTemplate.postForEntity(paymentUrl, paymentReq, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Payment TCC prepare failed", e);
        }

        return orderRepository.findById(orderId).orElse(null);
    }

    public Order handleCreateSaga(CreateOrderCommand command) {
        String txId = UUID.randomUUID().toString();
        Order order = new Order();
        order.setProductId(command.getProductId());
        order.setQuantity(command.getQuantity());
        order.setPrice(command.getPrice());
        order.setStatus("APPROVED");
        order.setReady(true);
        order.setTxId(txId);
        orderRepository.save(order);

        Map<String, Object> event = new HashMap<>();
        event.put("orderId", order.getId());
        event.put("productId", order.getProductId());
        event.put("quantity", order.getQuantity());
        event.put("price", order.getPrice());
        event.put("txId", txId);
        event.put("type", "ORDER_CREATED");
        event.put("simulatePaymentError", command.getSimulatePaymentError());

        kafkaTemplate.send("order-events", txId, event);
        return order;
    }

    @Transactional
    public void handleCompensate(CompensateOrderCommand command) {
        orderRepository.findByTxId(command.getTxId()).ifPresent(originalOrder -> {
            Order compensationOrder = new Order();
            compensationOrder.setProductId(originalOrder.getProductId());
            compensationOrder.setQuantity(originalOrder.getQuantity() * -1);
            compensationOrder.setPrice(originalOrder.getPrice());
            compensationOrder.setStatus("COMPENSATED");
            compensationOrder.setReady(true);
            compensationOrder.setTxId(command.getTxId() + "-compensation");
            orderRepository.save(compensationOrder);
        });
    }

    @Transactional
    public Order handleCreateSagaOrchestration(CreateOrderCommand command) {
        Order order = new Order();
        order.setProductId(command.getProductId());
        order.setQuantity(command.getQuantity());
        order.setPrice(command.getPrice());
        order.setStatus("PENDING");
        order.setReady(false);
        order.setTxId(UUID.randomUUID().toString() + "-orch");
        orderRepository.save(order);

        String paymentUrl = paymentServiceUrl + "/api/payments/saga/process";
        Map<String, Object> paymentReq = new HashMap<>();
        paymentReq.put("orderId", order.getId());
        paymentReq.put("amount", command.getPrice().multiply(new BigDecimal(command.getQuantity())));
        paymentReq.put("simulatePaymentError", command.getSimulatePaymentError());

        try {
            restTemplate.postForEntity(paymentUrl, paymentReq, Map.class);
            order.setStatus("APPROVED");
            order.setReady(true);
            orderRepository.save(order);
        } catch (Exception e) {
            String compensateUrl = paymentServiceUrl + "/api/payments/saga/compensate";
            try {
                restTemplate.postForEntity(compensateUrl, paymentReq, Map.class);
            } catch (Exception ex) {
            }
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            throw new RuntimeException("Saga Orchestration failed. Transaction rolled back.", e);
        }
        return order;
    }

    @Transactional
    public Order handleCreateSagaForwardRecovery(CreateOrderCommand command) {
        Order order = new Order();
        order.setProductId(command.getProductId());
        order.setQuantity(command.getQuantity());
        order.setPrice(command.getPrice());
        order.setStatus("PENDING");
        order.setReady(false);
        order.setTxId(UUID.randomUUID().toString() + "-forward");
        orderRepository.save(order);

        String paymentUrl = paymentServiceUrl + "/api/payments/saga/process";
        Map<String, Object> paymentReq = new HashMap<>();
        paymentReq.put("orderId", order.getId());
        paymentReq.put("amount", command.getPrice().multiply(new BigDecimal(command.getQuantity())));
        paymentReq.put("simulatePaymentError", command.getSimulatePaymentError());

        try {
            restTemplate.postForEntity(paymentUrl, paymentReq, Map.class);
            order.setStatus("APPROVED");
            order.setReady(true);
            orderRepository.save(order);
        } catch (Exception e) {
            order.setStatus("AWAITING_PAYMENT");
            orderRepository.save(order);
        }
        return order;
    }

    @GlobalTransactional(name = "create-order-at", rollbackFor = Exception.class)
    public Order handleCreateAt(CreateOrderCommand command) {
        Order order = new Order();
        order.setProductId(command.getProductId());
        order.setQuantity(command.getQuantity());
        order.setPrice(command.getPrice());
        order.setStatus("PENDING");
        order.setReady(false);
        order.setTxId(RootContext.getXID());
        orderRepository.save(order);

        String paymentUrl = paymentServiceUrl + "/api/payments/at";
        Map<String, Object> paymentReq = new HashMap<>();
        paymentReq.put("orderId", order.getId());
        paymentReq.put("amount", command.getPrice().multiply(new BigDecimal(command.getQuantity())));
        paymentReq.put("simulatePaymentError", command.getSimulatePaymentError());

        try {
            restTemplate.postForEntity(paymentUrl, paymentReq, Map.class);
            order.setStatus("APPROVED");
            order.setReady(true);
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Payment AT failed, transaction rolled back.", e);
        }
        return order;
    }

    @Transactional
    public Order handleCreateOrderPureAt(CreateOrderCommand command) {
        Order order = new Order();
        order.setProductId(command.getProductId());
        order.setQuantity(command.getQuantity());
        order.setPrice(command.getPrice());
        order.setStatus("APPROVED");
        order.setReady(true);
        order.setTxId(RootContext.getXID());
        orderRepository.save(order);
        return order;
    }

    @Transactional
    public Order handleCreateOrderPureSaga(CreateOrderCommand command) {
        Order order = new Order();
        order.setProductId(command.getProductId());
        order.setQuantity(command.getQuantity());
        order.setPrice(command.getPrice());
        order.setStatus("PENDING");
        order.setReady(false);
        order.setTxId(UUID.randomUUID().toString() + "-saga-orch");
        orderRepository.save(order);
        return order;
    }

    @Transactional
    public void handleUpdateOrderStatus(Long orderId, String status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            if ("APPROVED".equals(status)) {
                order.setReady(true);
            }
            orderRepository.save(order);
        });
    }

    @Transactional
    public Long handleCreateOrderTccPrepare(CreateOrderCommand command) {
        return orderTccAction.prepare(null, command.getProductId(), command.getQuantity(), command.getPrice());
    }

    @Transactional
    public void handleCleanAll() {
        orderRepository.deleteAll();
    }
}



