package com.safebank.account.infrastructure.controller;

import com.safebank.account.application.AccountService;
import com.safebank.account.application.dto.AccountResponse;
import com.safebank.auth.domain.User;
import com.safebank.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // permitimos que angular consulte este endpoint
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @GetMapping("/my-account")
    public ResponseEntity<AccountResponse> getMyAccount(Authentication authentication) {
        // gracias al JwtAuthenticationFilter, la variable authentication trae el email del token verificado
        String userEmail = authentication.getName();

        // buscamos al usuario por su email para obtener su id interno
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("usuario no encontrado"));

        // pedimos la cuenta de ese id y la devolvemos en formato json
        return ResponseEntity.ok(accountService.getAccountByUserId(user.getId()));
    }
}