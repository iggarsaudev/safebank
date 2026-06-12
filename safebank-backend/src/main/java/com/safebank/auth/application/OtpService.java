package com.safebank.auth.application;

import com.safebank.auth.domain.OtpToken;
import com.safebank.auth.domain.repository.OtpTokenRepository;
import com.safebank.auth.domain.User;
import com.safebank.auth.domain.repository.UserRepository;
import com.safebank.notification.application.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void generateAndSendOtp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Invalidar códigos anteriores del usuario por seguridad
        otpTokenRepository.findTopByUserIdAndUsedFalseOrderByExpiresAtDesc(userId)
                .ifPresent(token -> {
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                });

        // 2. Generar código aleatorio de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 3. Guardar en base de datos (Caduca en 5 minutos)
        OtpToken otpToken = OtpToken.builder()
                .userId(userId)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        otpTokenRepository.save(otpToken);

        // 4. Enviar el código por email
        emailService.sendOtpEmail(user.getEmail(), code);
    }

    @Transactional(readOnly = true)
    public boolean isOtpValid(Long userId, String code) {
        if (code == null || code.isBlank()) return false;

        OtpToken token = otpTokenRepository.findTopByUserIdAndUsedFalseOrderByExpiresAtDesc(userId)
                .orElse(null);

        // Verificaciones de seguridad estrictas
        if (token == null) return false;
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) return false; // Caducado
        if (!token.getCode().equals(code)) return false; // No coincide

        return true;
    }

    @Transactional
    public void markOtpAsUsed(Long userId) {
        otpTokenRepository.findTopByUserIdAndUsedFalseOrderByExpiresAtDesc(userId)
                .ifPresent(token -> {
                    token.setUsed(true);
                    otpTokenRepository.save(token);
                });
    }
}