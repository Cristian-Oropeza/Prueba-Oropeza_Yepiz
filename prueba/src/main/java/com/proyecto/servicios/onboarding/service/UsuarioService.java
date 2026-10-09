package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.response.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {
    Page<UsuarioResponse> filtrar(String correo, Boolean activo, Pageable pageable);
    UsuarioResponse actualizarEstatus(Long clienteId, Boolean activo);
}
