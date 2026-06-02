package com.safebank.auth.domain.repository;

import com.safebank.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // buscamos un usuario por su email para el proceso de login
    Optional<User> findByEmail(String email);
    
    // verificamos si un correo ya existe al momento de registrar
    boolean existsByEmail(String email);
}