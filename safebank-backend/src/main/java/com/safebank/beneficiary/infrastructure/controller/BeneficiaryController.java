package com.safebank.beneficiary.infrastructure.controller;

import com.safebank.auth.domain.User;
import com.safebank.auth.domain.repository.UserRepository;
import com.safebank.beneficiary.application.BeneficiaryService;
import com.safebank.beneficiary.application.dto.BeneficiaryRequest;
import com.safebank.beneficiary.domain.Beneficiary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/beneficiaries")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Beneficiary>> getMyBeneficiaries(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(beneficiaryService.getMyBeneficiaries(user.getId()));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addBeneficiary(@Valid @RequestBody BeneficiaryRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        beneficiaryService.addBeneficiary(user.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Contacto guardado correctamente en tu agenda"));
    }
}