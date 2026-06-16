package com.example.orderservice.presentation.grpc;

import com.example.orderservice.application.command.CreateOrderCommand;
import com.example.orderservice.application.command.OrderCommandHandler;
import com.example.orderservice.domain.model.Order;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;

@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    @Autowired
    private OrderCommandHandler orderCommandHandler;

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        try {
            CreateOrderCommand command = new CreateOrderCommand(
                    request.getProductId(),
                    request.getQuantity(),
                    BigDecimal.valueOf(request.getPrice()),
                    request.getSimulatePaymentError()
            );
            Order order = orderCommandHandler.handleCreateOrderPureAt(command);

            OrderResponse response = OrderResponse.newBuilder()
                    .setId(order.getId())
                    .setProductId(order.getProductId())
                    .setQuantity(order.getQuantity())
                    .setPrice(order.getPrice().doubleValue())
                    .setStatus(order.getStatus())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}
