package com.safebank.account.application;

import com.safebank.account.application.dto.AccountResponse;
import com.safebank.account.domain.Account;
import com.safebank.account.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public void createAccountForUser(Long userId) {
        // generamos un iban único para el nuevo usuario
        String iban = generateUniqueIban();
        
        Account account = Account.builder()
                .userId(userId)
                .iban(iban)
                .balance(BigDecimal.ZERO) // toda cuenta nueva nace con 0 euros
                .build();
                
        accountRepository.save(account);
    }

    public AccountResponse getAccountByUserId(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("no se ha encontrado ninguna cuenta para este usuario"));
                
        return new AccountResponse(account.getIban(), account.getBalance());
    }

    private String generateUniqueIban() {
        Random random = new Random();
        String iban;
        
        do {
            StringBuilder sb = new StringBuilder("ES");
            // generamos 22 dígitos aleatorios estándar para un iban español
            for (int i = 0; i < 22; i++) {
                sb.append(random.nextInt(10));
            }
            iban = sb.toString();
            // verificamos en base de datos que no exista ya por una casualidad extrema
        } while (accountRepository.existsByIban(iban));
        
        return iban;
    }
}