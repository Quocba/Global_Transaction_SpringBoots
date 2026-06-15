package com.example.orderservice.infrastructure.tcc;

import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class OrderTccActionImpl implements OrderTccAction {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public Long prepare(BusinessActionContext context, String productId, Integer quantity, BigDecimal price) {
        String xid = context.getXid();
        Order order = new Order();
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setStatus("PENDING");
        order.setReady(false);
        order.setTxId(xid);
        orderRepository.save(order);
        return order.getId();
    }

    @Override
    @Transactional
    public boolean commit(BusinessActionContext context) {
        String xid = context.getXid();
        orderRepository.findByTxId(xid).ifPresent(order -> {
            order.setReady(true);
            order.setStatus("APPROVED");
            orderRepository.save(order);
        });
        return true;
    }

    @Override
    @Transactional
    public boolean rollback(BusinessActionContext context) {
        String xid = context.getXid();
        orderRepository.findByTxId(xid).ifPresent(order -> {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        });
        return true;
    }
}
