package com.proyecto.servicios.onboarding.service.impl;

import com.proyecto.servicios.onboarding.dto.response.UsuarioResponse;
import com.proyecto.servicios.onboarding.entity.Usuario;
import com.proyecto.servicios.onboarding.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.onboarding.mapper.UsuarioMapper;
import com.proyecto.servicios.onboarding.repository.UsuarioRepository;
import com.proyecto.servicios.onboarding.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;

    @Override @Transactional(readOnly = true)
    public Page<UsuarioResponse> filtrar(String correo, Boolean activo, Pageable p) {
        if (correo != null && !correo.isBlank()) {
            return usuarioRepository.findByCorreoContainingIgnoreCase(correo.toLowerCase(), p).map(mapper::aResponse);
        }
        if (activo != null) {
            return usuarioRepository.findByActivo(activo, p).map(mapper::aResponse);
        }
        return usuarioRepository.findAll(p).map(mapper::aResponse);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarEstatus(Long clienteId, Boolean activo) {
        Usuario u = usuarioRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado para cliente " + clienteId));
        u.setActivo(activo);
        return mapper.aResponse(usuarioRepository.save(u));
    }
}
