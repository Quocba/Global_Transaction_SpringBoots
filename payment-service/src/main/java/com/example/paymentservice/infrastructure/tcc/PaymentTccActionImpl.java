package com.example.paymentservice.infrastructure.tcc;

import com.example.paymentservice.domain.model.Payment;
import com.example.paymentservice.domain.repository.PaymentRepository;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class PaymentTccActionImpl implements PaymentTccAction {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    @Transactional
    public boolean prepare(BusinessActionContext context, Long orderId, BigDecimal amount, Boolean simulatePaymentError) {
        if (Boolean.TRUE.equals(simulatePaymentError)) {
            throw new RuntimeException("Simulated payment error in TCC phase");
        }
        String xid = context != null ? context.getXid() : io.seata.core.context.RootContext.getXID();
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(amount);
        payment.setStatus("PENDING");
        payment.setReady(false);
        payment.setTxId(xid);
        paymentRepository.save(payment);
        return true;
    }

    @Override
    @Transactional
    public boolean commit(BusinessActionContext context) {
        String xid = context.getXid();
        paymentRepository.findByTxId(xid).ifPresent(payment -> {
            payment.setReady(true);
            payment.setStatus("APPROVED");
            paymentRepository.save(payment);
        });
        return true;
    }

    @Override
    @Transactional
    public boolean rollback(BusinessActionContext context) {
        String xid = context.getXid();
        paymentRepository.findByTxId(xid).ifPresent(payment -> {
            payment.setStatus("CANCELLED");
            paymentRepository.save(payment);
        });
        return true;
    }
}
