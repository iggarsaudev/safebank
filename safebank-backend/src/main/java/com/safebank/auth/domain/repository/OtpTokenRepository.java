package com.safebank.auth.domain.repository;

import com.safebank.auth.domain.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    // Busca el último código activo que se le haya generado al usuario
    Optional<OtpToken> findTopByUserIdAndUsedFalseOrderByExpiresAtDesc(Long userId);
}