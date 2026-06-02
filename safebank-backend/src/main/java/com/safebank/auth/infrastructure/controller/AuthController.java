package com.safebank.auth.infrastructure.controller;

import com.safebank.auth.application.AuthService;
import com.safebank.auth.application.dto.AuthResponse;
import com.safebank.auth.application.dto.LoginRequest;
import com.safebank.auth.application.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // habilitamos cors para que nuestro angular pueda consumir la api
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // delegamos la lógica de registro al servicio de aplicación
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // delegamos la lógica de inicio de sesión al servicio de aplicación
        return ResponseEntity.ok(authService.login(request));
    }
}