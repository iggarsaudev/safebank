package com.safebank.account.domain.repository;

import com.safebank.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    // buscaremos la cuenta de un usuario a través de su id
    Optional<Account> findByUserId(Long userId);
    
    // útil para comprobar que no generamos un iban duplicado por accidente
    boolean existsByIban(String iban);

    // buscamos la cuenta de destino a través del iban
    Optional<Account> findByIban(String iban);
}