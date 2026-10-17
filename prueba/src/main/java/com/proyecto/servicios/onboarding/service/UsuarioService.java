package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.request.UsuarioActualizarRequest;
import com.proyecto.servicios.onboarding.dto.request.UsuarioConsultaRequest;
import com.proyecto.servicios.onboarding.dto.response.UsuarioResponse;
import org.springframework.data.domain.Page;

public interface UsuarioService {
    Page<UsuarioResponse> consultar(UsuarioConsultaRequest req);
    UsuarioResponse actualizarEstatus(UsuarioActualizarRequest req);
}
