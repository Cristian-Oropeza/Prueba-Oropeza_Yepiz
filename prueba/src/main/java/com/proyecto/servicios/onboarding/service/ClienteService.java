package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.request.ClienteAltaRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.ClienteAltaResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResumenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ClienteService {
    ClienteAltaResponse registrar(ClienteAltaRequest req);
    ClienteResponse obtenerPorId(Long id);
    ClienteResponse obtenerPorCurp(String curp);
    ClienteResponse obtenerPorRfc(String rfc);
    ClienteResponse obtenerPorCorreo(String correo);
    ClienteResponse obtenerPorNumeroCuenta(String numeroCuenta);
    Page<ClienteResumenResponse> listar(Pageable pageable, Boolean activo);
    Page<ClienteResumenResponse> buscarPorNombre(String nombre, Pageable pageable);
    Page<ClienteResumenResponse> buscarPorApellidoPaterno(String ap, Pageable pageable);
    Page<ClienteResumenResponse> buscarPorApellidoMaterno(String am, Pageable pageable);
    Page<ClienteResumenResponse> buscarPorRangoFechas(LocalDate desde, LocalDate hasta, Pageable pageable);
    ClienteResponse actualizarParcial(Long id, ClienteUpdateRequest req);
    void darDeBaja(Long id);
}
