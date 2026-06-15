package com.example.orderservice.application.query;

import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderQueryHandler {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> handle(GetAllOrdersQuery query) {
        return orderRepository.findAll();
    }
}
