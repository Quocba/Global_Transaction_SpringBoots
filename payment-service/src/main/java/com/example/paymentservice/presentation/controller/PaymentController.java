package com.example.paymentservice.presentation.controller;

import com.example.paymentservice.application.command.CompensatePaymentCommand;
import com.example.paymentservice.application.command.PaymentCommandHandler;
import com.example.paymentservice.application.command.ProcessPaymentCommand;
import com.example.paymentservice.application.query.GetAllPaymentsQuery;
import com.example.paymentservice.application.query.PaymentQueryHandler;
import com.example.paymentservice.domain.model.Payment;
import com.example.paymentservice.presentation.dto.ApiResponse;
import com.example.paymentservice.presentation.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentCommandHandler paymentCommandHandler;

    @Autowired
    private PaymentQueryHandler paymentQueryHandler;

    @PostMapping("/tcc/prepare")
    public ResponseEntity<ApiResponse<String>> preparePaymentTcc(@RequestBody PaymentRequest request) {
        try {
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getSimulatePaymentError()
            );
            paymentCommandHandler.handlePrepareTcc(command);
            return ResponseEntity.ok(new ApiResponse<>(200, "Prepare Success", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, e.getMessage(), null));
        }
    }

    @GetMapping
    public ApiResponse<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentQueryHandler.handle(new GetAllPaymentsQuery());
        return new ApiResponse<>(200, "Success", payments);
    }

    @PostMapping("/saga/process")
    public ResponseEntity<ApiResponse<String>> processPaymentRest(@RequestBody PaymentRequest request) {
        try {
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getSimulatePaymentError()
            );
            paymentCommandHandler.handleProcessRest(command);
            return ResponseEntity.ok(new ApiResponse<>(200, "Payment Success", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, e.getMessage(), null));
        }
    }

    @PostMapping("/saga/compensate")
    public ResponseEntity<ApiResponse<String>> compensatePaymentRest(@RequestBody PaymentRequest request) {
        try {
            CompensatePaymentCommand command = new CompensatePaymentCommand(
                    request.getOrderId(),
                    request.getAmount()
            );
            paymentCommandHandler.handleCompensateRest(command);
            return ResponseEntity.ok(new ApiResponse<>(200, "Compensate Success", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, e.getMessage(), null));
        }
    }

    @PostMapping("/at")
    public ResponseEntity<ApiResponse<String>> processPaymentAt(@RequestBody PaymentRequest request) {
        try {
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getSimulatePaymentError()
            );
            paymentCommandHandler.handleProcessAt(command);
            return ResponseEntity.ok(new ApiResponse<>(200, "Payment Success", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, e.getMessage(), null));
        }
    }

    @DeleteMapping("/clean-all")
    public ResponseEntity<ApiResponse<String>> cleanAllPayments() {
        try {
            paymentCommandHandler.handleCleanAll();
            return ResponseEntity.ok(new ApiResponse<>(200, "Clean all payments success", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(500, e.getMessage(), null));
        }
    }
}

