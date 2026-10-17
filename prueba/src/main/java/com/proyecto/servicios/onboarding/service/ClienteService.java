package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.request.ClienteAltaRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteConsultaRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteIdRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.ClienteAltaResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResumenResponse;
import org.springframework.data.domain.Page;

public interface ClienteService {
    ClienteAltaResponse registrar(ClienteAltaRequest req);
    Page<ClienteResumenResponse> consultar(ClienteConsultaRequest req);
    ClienteResponse actualizarParcial(ClienteUpdateRequest req);
    void darDeBaja(ClienteIdRequest req);
}
