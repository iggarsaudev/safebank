package com.safebank.beneficiary.application;

import com.safebank.beneficiary.application.dto.BeneficiaryRequest;
import com.safebank.beneficiary.domain.Beneficiary;
import com.safebank.beneficiary.domain.repository.BeneficiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    @Transactional(readOnly = true)
    public List<Beneficiary> getMyBeneficiaries(Long userId) {
        // Devuelve la lista ordenada alfabéticamente
        return beneficiaryRepository.findByUserIdOrderByNameAsc(userId);
    }

    @Transactional
    public void addBeneficiary(Long userId, BeneficiaryRequest request) {
        // Regla de negocio: No guardar duplicados
        if (beneficiaryRepository.existsByUserIdAndIban(userId, request.iban())) {
            throw new RuntimeException("Ya tienes este IBAN guardado en tu agenda");
        }

        // Construimos la entidad y la guardamos
        Beneficiary beneficiary = Beneficiary.builder()
                .userId(userId)
                .name(request.name())
                .iban(request.iban().toUpperCase()) // Guardamos el IBAN siempre en mayúsculas por seguridad
                .build();

        beneficiaryRepository.save(beneficiary);
    }
}