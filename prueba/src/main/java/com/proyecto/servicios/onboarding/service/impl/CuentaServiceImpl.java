package com.proyecto.servicios.onboarding.service.impl;

import com.proyecto.servicios.onboarding.dto.request.CuentaCreateRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.CuentaResponse;
import com.proyecto.servicios.onboarding.dto.response.CuentaSaldoResponse;
import com.proyecto.servicios.onboarding.entity.Cliente;
import com.proyecto.servicios.onboarding.entity.Cuenta;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import com.proyecto.servicios.onboarding.exception.*;
import com.proyecto.servicios.onboarding.mapper.ClienteMapper;
import com.proyecto.servicios.onboarding.repository.ClienteRepository;
import com.proyecto.servicios.onboarding.repository.CuentaRepository;
import com.proyecto.servicios.onboarding.service.CuentaService;
import com.proyecto.servicios.onboarding.service.NumeroCuentaGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final NumeroCuentaGenerator generator;
    private final ClienteMapper mapper;

    @Value("${app.cuenta.saldo-inicial:0.00}")
    private BigDecimal saldoDefault;

    @Override
    @Transactional
    public CuentaResponse crear(CuentaCreateRequest req) {
        Cliente cliente = clienteRepository.findById(req.clienteId())
                .orElseThrow(() -> ClienteNoEncontradoException.porId(req.clienteId()));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw ValidacionNegocioException.clienteInactivo();
        }
        BigDecimal saldo = req.saldoInicial() != null ? req.saldoInicial() : saldoDefault;
        if (saldo.signum() < 0) throw ValidacionNegocioException.saldoInvalido();

        Cuenta ct = Cuenta.builder()
                .numeroCuenta(generator.generar())
                .cliente(cliente)
                .saldo(saldo)
                .estatus(EstatusCuenta.ACTIVA)
                .build();
        return mapper.aCuentaResponse(cuentaRepository.save(ct));
    }

    @Override @Transactional(readOnly = true)
    public CuentaResponse obtenerPorNumero(String n) {
        return mapper.aCuentaResponse(cuentaRepository.findByNumeroCuenta(n)
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(n)));
    }

    @Override @Transactional(readOnly = true)
    public List<CuentaResponse> obtenerPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId))
            throw ClienteNoEncontradoException.porId(clienteId);
        return cuentaRepository.findByClienteId(clienteId).stream()
                .map(mapper::aCuentaResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public Page<CuentaResponse> buscarPorEstatus(EstatusCuenta estatus, Pageable p) {
        return cuentaRepository.findByEstatus(estatus, p).map(mapper::aCuentaResponse);
    }

    @Override @Transactional(readOnly = true)
    public CuentaSaldoResponse obtenerSaldo(String n) {
        Cuenta ct = cuentaRepository.findByNumeroCuenta(n)
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(n));
        return new CuentaSaldoResponse(ct.getNumeroCuenta(), ct.getSaldo(), ct.getEstatus());
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(String n, CuentaUpdateRequest req) {
        Cuenta ct = cuentaRepository.findByNumeroCuenta(n)
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(n));
        if (req.estatus() != null) {
            // Regla: sólo clientes activos pueden tener cuentas ACTIVAS
            if (req.estatus() == EstatusCuenta.ACTIVA && !Boolean.TRUE.equals(ct.getCliente().getActivo())) {
                throw ValidacionNegocioException.clienteInactivo();
            }
            ct.setEstatus(req.estatus());
        }
        return mapper.aCuentaResponse(cuentaRepository.save(ct));
    }
}
