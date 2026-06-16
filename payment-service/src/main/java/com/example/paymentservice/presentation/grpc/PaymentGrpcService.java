package com.example.paymentservice.presentation.grpc;

import com.example.paymentservice.application.command.ProcessPaymentCommand;
import com.example.paymentservice.application.command.PaymentCommandHandler;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;

@GrpcService
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {

    @Autowired
    private PaymentCommandHandler paymentCommandHandler;

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        try {
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    request.getOrderId(),
                    BigDecimal.valueOf(request.getAmount()),
                    request.getSimulatePaymentError()
            );
            paymentCommandHandler.handleProcessAt(command);

            PaymentResponse response = PaymentResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Payment Success")
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
