package com.safebank.auth.application;

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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // validamos si el email ya se encuentra registrado en el sistema
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("el correo electrónico ya está registrado");
        }

        // creamos la entidad encriptando la contraseña antes de persistir
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        // generamos el token de acceso para el usuario recién creado
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "usuario registrado con éxito");
    }

    public AuthResponse login(LoginRequest request) {
        // buscamos al usuario por email y lanzamos excepción si no existe
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("credenciales incorrectas"));

        // verificamos si la contraseña coincide con el hash guardado en base de datos
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("credenciales incorrectas");
        }

        // generamos el token si la autenticación es correcta
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "inicio de sesión correcto");
    }
}