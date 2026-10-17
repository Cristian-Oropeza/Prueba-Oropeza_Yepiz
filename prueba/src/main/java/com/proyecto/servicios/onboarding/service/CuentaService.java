package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.request.CuentaConsultaRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaCreateRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.CuentaResponse;
import org.springframework.data.domain.Page;

public interface CuentaService {
    CuentaResponse crear(CuentaCreateRequest req);
    Page<CuentaResponse> consultar(CuentaConsultaRequest req);
    CuentaResponse actualizarParcial(CuentaUpdateRequest req);
}
