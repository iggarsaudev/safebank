package com.safebank.auth.application.dto;

public record AuthResponse(
        String token,
        String message
) {}