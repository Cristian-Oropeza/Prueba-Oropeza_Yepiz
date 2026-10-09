package com.proyecto.servicios.onboarding.service.impl;

import com.proyecto.servicios.onboarding.dto.request.LoginRequest;
import com.proyecto.servicios.onboarding.dto.response.LoginResponse;
import com.proyecto.servicios.onboarding.entity.Usuario;
import com.proyecto.servicios.onboarding.exception.CredencialesInvalidasException;
import com.proyecto.servicios.onboarding.exception.UsuarioInactivoException;
import com.proyecto.servicios.onboarding.repository.UsuarioRepository;
import com.proyecto.servicios.onboarding.security.JwtService;
import com.proyecto.servicios.onboarding.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        String correo = req.correo().toLowerCase().trim();
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!Boolean.TRUE.equals(u.getActivo()) || !Boolean.TRUE.equals(u.getCliente().getActivo())) {
            throw new UsuarioInactivoException();
        }
        if (!passwordEncoder.matches(req.password(), u.getPassword())) {
            log.warn("Login fallido para correo={}", correo);
            throw new CredencialesInvalidasException();
        }

        String token = jwtService.generarToken(u.getCliente().getId(), u.getCorreo());
        return new LoginResponse(
                token, "Bearer",
                jwtService.getExpirationSeconds(),
                u.getCliente().getId(), u.getCorreo()
        );
    }
}
