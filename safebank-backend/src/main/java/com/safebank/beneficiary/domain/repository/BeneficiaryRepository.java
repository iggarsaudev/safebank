package com.safebank.beneficiary.domain.repository;

import com.safebank.beneficiary.domain.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    
    // Trae los contactos de un usuario ordenados de la A a la Z
    List<Beneficiary> findByUserIdOrderByNameAsc(Long userId);
    
    // Sirve para validar si un usuario ya tiene este IBAN guardado en su agenda
    boolean existsByUserIdAndIban(Long userId, String iban);
}