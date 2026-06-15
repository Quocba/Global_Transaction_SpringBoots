package com.example.coordinatorservice.presentation.controller;

import com.example.coordinatorservice.application.service.TransactionOrchestrator;
import com.example.coordinatorservice.presentation.dto.ApiResponse;
import com.example.coordinatorservice.presentation.dto.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/coordinator")
public class CoordinatorController {

    @Autowired
    private TransactionOrchestrator transactionOrchestrator;

    @PostMapping("/at")
    public ApiResponse<Map<String, Object>> createTransactionAt(@RequestBody TransactionRequest request) {
        try {
            Map<String, Object> data = transactionOrchestrator.executeGlobalTransactionAt(request);
            return new ApiResponse<>(200, "Global Transaction Success (AT Mode)", data);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @PostMapping("/saga")
    public ApiResponse<Map<String, Object>> createTransactionSaga(@RequestBody TransactionRequest request) {
        try {
            Map<String, Object> data = transactionOrchestrator.executeSagaOrchestration(request);
            return new ApiResponse<>(200, "Global Transaction Success (Saga Mode)", data);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @PostMapping("/saga-orch")
    public ApiResponse<Map<String, Object>> createTransactionSagaOrch(@RequestBody TransactionRequest request) {
        try {
            Map<String, Object> data = transactionOrchestrator.executeSagaOrchestration(request);
            return new ApiResponse<>(200, "Global Transaction Success (Saga Orchestration)", data);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @PostMapping("/tcc")
    public ApiResponse<Map<String, Object>> createTransactionTcc(@RequestBody TransactionRequest request) {
        try {
            Map<String, Object> data = transactionOrchestrator.executeGlobalTransactionTcc(request);
            return new ApiResponse<>(200, "Global Transaction Success (TCC Mode)", data);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @PostMapping("/saga-choreo")
    public ApiResponse<Map<String, Object>> createTransactionSagaChoreo(@RequestBody TransactionRequest request) {
        try {
            Map<String, Object> data = transactionOrchestrator.executeSagaChoreography(request);
            return new ApiResponse<>(200, "Global Transaction Success (Saga Choreography)", data);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }

    @DeleteMapping("/clean-all")
    public ApiResponse<String> cleanAll() {
        try {
            transactionOrchestrator.cleanAllDatabases();
            return new ApiResponse<>(200, "Clean all databases success", null);
        } catch (Exception e) {
            return new ApiResponse<>(500, e.getMessage(), null);
        }
    }
}

