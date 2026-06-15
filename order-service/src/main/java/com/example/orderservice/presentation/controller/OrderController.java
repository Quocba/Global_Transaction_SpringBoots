package com.example.orderservice.presentation.controller;

import com.example.orderservice.application.command.CreateOrderCommand;
import com.example.orderservice.application.command.OrderCommandHandler;
import com.example.orderservice.application.query.GetAllOrdersQuery;
import com.example.orderservice.application.query.OrderQueryHandler;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.presentation.dto.ApiResponse;
import com.example.orderservice.presentation.dto.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderCommandHandler orderCommandHandler;

    @Autowired
    private OrderQueryHandler orderQueryHandler;

    @PostMapping("/tcc")
    public ApiResponse<Order> createOrderTcc(@RequestBody OrderRequest request) {
        try {
            CreateOrderCommand command = new CreateOrderCommand(
                    request.getProductId(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getSimulatePaymentError()
            );
            Order order = orderCommandHandler.handleCreateTcc(command);
            return new ApiResponse<>(200, "Success", order);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @PostMapping("/saga")
    public ApiResponse<Order> createOrderSaga(@RequestBody OrderRequest request) {
        try {
            CreateOrderCommand command = new CreateOrderCommand(
                    request.getProductId(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getSimulatePaymentError()
            );
            Order order = orderCommandHandler.handleCreateSaga(command);
            return new ApiResponse<>(200, "Success", order);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @GetMapping
    public ApiResponse<List<Order>> getAllOrders() {
        List<Order> orders = orderQueryHandler.handle(new GetAllOrdersQuery());
        return new ApiResponse<>(200, "Success", orders);
    }

    @PostMapping("/saga/orchestration")
    public ApiResponse<Order> createOrderSagaOrchestration(@RequestBody OrderRequest request) {
        try {
            CreateOrderCommand command = new CreateOrderCommand(
                    request.getProductId(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getSimulatePaymentError()
            );
            Order order = orderCommandHandler.handleCreateSagaOrchestration(command);
            return new ApiResponse<>(200, "Success", order);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @PostMapping("/saga/forward-recovery")
    public ApiResponse<Order> createOrderSagaForwardRecovery(@RequestBody OrderRequest request) {
        try {
            CreateOrderCommand command = new CreateOrderCommand(
                    request.getProductId(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getSimulatePaymentError()
            );
            Order order = orderCommandHandler.handleCreateSagaForwardRecovery(command);
            return new ApiResponse<>(200, "Success", order);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @PostMapping("/at")
    public ApiResponse<Order> createOrderAt(@RequestBody OrderRequest request) {
        try {
            CreateOrderCommand command = new CreateOrderCommand(
                    request.getProductId(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getSimulatePaymentError()
            );
            Order order = orderCommandHandler.handleCreateAt(command);
            return new ApiResponse<>(200, "Success", order);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }
}
