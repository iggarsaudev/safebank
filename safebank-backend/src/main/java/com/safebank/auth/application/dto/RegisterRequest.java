package com.safebank.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// usamos un record de java para crear un dto inmutable de forma limpia
public record RegisterRequest(
        @NotBlank 
        String firstName,
        
        @NotBlank 
        String lastName,
        
        @NotBlank 
        @Email 
        String email,
        
        @NotBlank 
        @Size(min = 6, message = "la contraseña debe tener al menos 6 caracteres") 
        String password
) {}