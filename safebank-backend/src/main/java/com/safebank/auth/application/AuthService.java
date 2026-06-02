package com.safebank.auth.application;

import com.safebank.account.application.AccountService;
import com.safebank.auth.application.dto.AuthResponse;
import com.safebank.auth.application.dto.LoginRequest;
import com.safebank.auth.application.dto.RegisterRequest;
import com.safebank.auth.domain.Role;
import com.safebank.auth.domain.User;
import com.safebank.auth.domain.repository.UserRepository;
import com.safebank.auth.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccountService accountService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("el correo electrónico ya está registrado");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        // spring data jpa actualiza el objeto 'user' insertando el id autogenerado
        user = userRepository.save(user);

        // ¡MAGIA! creamos la cuenta bancaria asociada a ese nuevo ID
        accountService.createAccountForUser(user.getId());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "usuario registrado con éxito");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("credenciales incorrectas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("credenciales incorrectas");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "inicio de sesión correcto");
    }
}