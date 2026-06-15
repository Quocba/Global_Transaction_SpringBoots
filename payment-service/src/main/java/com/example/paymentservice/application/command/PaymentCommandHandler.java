package com.example.paymentservice.application.command;

import com.example.paymentservice.domain.model.Payment;
import com.example.paymentservice.domain.repository.PaymentRepository;
import com.example.paymentservice.infrastructure.tcc.PaymentTccAction;
import io.seata.core.context.RootContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentCommandHandler {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentTccAction paymentTccAction;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void handlePrepareTcc(ProcessPaymentCommand command) {
        paymentTccAction.prepare(null, command.getOrderId(), command.getAmount(), command.getSimulatePaymentError());
    }

    @Transactional
    public void handleProcessSaga(Long orderId, BigDecimal price, Integer quantity, String txId, Boolean simulatePaymentError) {
        BigDecimal amount = price.multiply(new BigDecimal(quantity));

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus("APPROVED");
        payment.setReady(true);
        payment.setTxId(txId);
        paymentRepository.save(payment);

        if (Boolean.TRUE.equals(simulatePaymentError)) {
            Payment compensationPayment = new Payment();
            compensationPayment.setOrderId(orderId);
            compensationPayment.setAmount(amount.negate());
            compensationPayment.setStatus("COMPENSATED");
            compensationPayment.setReady(true);
            compensationPayment.setTxId(txId + "-compensation");
            paymentRepository.save(compensationPayment);

            Map<String, Object> failedEvent = new HashMap<>();
            failedEvent.put("orderId", orderId);
            failedEvent.put("txId", txId);
            failedEvent.put("status", "PAYMENT_FAILED");
            kafkaTemplate.send("payment-events", txId, failedEvent);
        } else {
            Map<String, Object> successEvent = new HashMap<>();
            successEvent.put("orderId", orderId);
            successEvent.put("txId", txId);
            successEvent.put("status", "PAYMENT_SUCCESS");
            kafkaTemplate.send("payment-events", txId, successEvent);
        }
    }

    @Transactional
    public void handleProcessRest(ProcessPaymentCommand command) {
        if (Boolean.TRUE.equals(command.getSimulatePaymentError())) {
            throw new RuntimeException("Simulated payment error in REST Saga phase");
        }
        Payment payment = new Payment();
        payment.setOrderId(command.getOrderId());
        payment.setAmount(command.getAmount());
        payment.setStatus("APPROVED");
        payment.setReady(true);
        payment.setTxId(command.getOrderId() + "-rest-saga");
        paymentRepository.save(payment);
    }

    @Transactional
    public void handleCompensateRest(CompensatePaymentCommand command) {
        Payment compensationPayment = new Payment();
        compensationPayment.setOrderId(command.getOrderId());
        compensationPayment.setAmount(command.getAmount().negate());
        compensationPayment.setStatus("COMPENSATED");
        compensationPayment.setReady(true);
        compensationPayment.setTxId(command.getOrderId() + "-rest-saga-compensation");
        paymentRepository.save(compensationPayment);
    }

    @Transactional
    public void handleProcessAt(ProcessPaymentCommand command) {
        if (Boolean.TRUE.equals(command.getSimulatePaymentError())) {
            throw new RuntimeException("Simulated payment error in AT phase");
        }
        Payment payment = new Payment();
        payment.setOrderId(command.getOrderId());
        payment.setAmount(command.getAmount());
        payment.setStatus("APPROVED");
        payment.setReady(true);
        payment.setTxId(RootContext.getXID());
        paymentRepository.save(payment);
    }
}
