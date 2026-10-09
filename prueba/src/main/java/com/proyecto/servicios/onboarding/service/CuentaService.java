package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.request.CuentaCreateRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.CuentaResponse;
import com.proyecto.servicios.onboarding.dto.response.CuentaSaldoResponse;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CuentaService {
    CuentaResponse crear(CuentaCreateRequest req);
    CuentaResponse obtenerPorNumero(String numeroCuenta);
    List<CuentaResponse> obtenerPorCliente(Long clienteId);
    Page<CuentaResponse> buscarPorEstatus(EstatusCuenta estatus, Pageable pageable);
    CuentaSaldoResponse obtenerSaldo(String numeroCuenta);
    CuentaResponse actualizarParcial(String numeroCuenta, CuentaUpdateRequest req);
}
