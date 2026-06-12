package com.safebank.transaction.infrastructure.controller;

import com.safebank.auth.domain.User;
import com.safebank.auth.domain.repository.UserRepository;
import com.safebank.transaction.application.PdfReceiptService;
import com.safebank.transaction.application.TransactionService;
import com.safebank.transaction.application.dto.TransactionRequest;
import com.safebank.transaction.domain.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // vital para que angular pueda comunicarse sin bloqueos cors
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final PdfReceiptService pdfReceiptService;
    private final com.safebank.auth.application.OtpService otpService;

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
    public ResponseEntity<Page<com.safebank.transaction.application.dto.TransactionHistoryResponse>> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size // Enviamos 5 transacciones por página por defecto
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        return ResponseEntity.ok(transactionService.getMyTransactions(user.getId(), page, size));
    }

    // Descarga el pdf
    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id, Authentication authentication) {
        // 1. Obtenemos el usuario autenticado
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        
        // 2. Obtenemos la transacción validando que sea suya
        var tx = transactionService.getTransactionReceipt(user.getId(), id);
        
        // 3. Generamos el chorro de bytes del PDF
        byte[] pdfBytes = pdfReceiptService.generateReceipt(tx);

        // 4. Preparamos las cabeceras HTTP para decirle al navegador "¡Cuidado, viene un archivo descargable!"
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "justificante-" + tx.id() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<com.safebank.transaction.domain.ScheduledTransfer>> getMyScheduledTransfers(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(transactionService.getMyScheduledTransfers(user.getId()));
    }

    @DeleteMapping("/scheduled/{id}")
    public ResponseEntity<Map<String, String>> cancelScheduledTransfer(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        transactionService.cancelScheduledTransfer(user.getId(), id);
        return ResponseEntity.ok(Map.of("message", "Pago programado cancelado correctamente"));
    }

    @PostMapping("/otp")
    public ResponseEntity<Map<String, String>> requestOtp(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();

        // Llamamos al motor que genera la clave y envía el correo
        otpService.generateAndSendOtp(user.getId());

        return ResponseEntity.ok(Map.of("message", "Código de seguridad enviado a tu correo electrónico"));
    }
}