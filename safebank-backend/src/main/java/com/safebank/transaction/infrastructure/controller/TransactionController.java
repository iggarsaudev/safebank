package com.safebank.transaction.infrastructure.controller;

import com.safebank.auth.domain.User;
import com.safebank.auth.domain.repository.UserRepository;
import com.safebank.transaction.application.TransactionService;
import com.safebank.transaction.application.dto.TransactionRequest;
import com.safebank.transaction.domain.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // vital para que angular pueda comunicarse sin bloqueos cors
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Map<String, String>> makeTransfer(@Valid @RequestBody TransactionRequest request, Authentication authentication) {
        // 1. extraemos al usuario autenticado usando el token jwt
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        // 2. delegamos la operación compleja al servicio
        transactionService.makeTransfer(user.getId(), request);

        // 3. devolvemos una respuesta de éxito en formato json (map.of crea un json clave-valor instantáneo)
        return ResponseEntity.ok(Map.of("message", "transferencia realizada con éxito"));
    }

    @GetMapping
    public ResponseEntity<List<com.safebank.transaction.application.dto.TransactionHistoryResponse>> getMyTransactions(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        // devolvemos el historial de transferencias en formato json
        return ResponseEntity.ok(transactionService.getMyTransactions(user.getId()));
    }
}