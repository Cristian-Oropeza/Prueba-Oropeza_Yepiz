package com.proyecto.servicios.onboarding.service.impl;

import com.proyecto.servicios.onboarding.dto.request.CuentaConsultaRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaCreateRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.CuentaResponse;
import com.proyecto.servicios.onboarding.entity.Cliente;
import com.proyecto.servicios.onboarding.entity.Cuenta;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import com.proyecto.servicios.onboarding.exception.*;
import com.proyecto.servicios.onboarding.mapper.ClienteMapper;
import com.proyecto.servicios.onboarding.repository.ClienteRepository;
import com.proyecto.servicios.onboarding.repository.CuentaRepository;
import com.proyecto.servicios.onboarding.service.CuentaService;
import com.proyecto.servicios.onboarding.service.NumeroCuentaGenerator;
import com.proyecto.servicios.onboarding.service.PaginacionFactory;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final NumeroCuentaGenerator generator;
    private final ClienteMapper mapper;
    private final PaginacionFactory paginacionFactory;

    @Value("${app.cuenta.saldo-inicial:0.00}")
    private BigDecimal saldoDefault;

    @Override
    @Transactional
    public CuentaResponse crear(CuentaCreateRequest req) {
        Cliente cliente = clienteRepository.findById(req.clienteId())
                .orElseThrow(() -> ClienteNoEncontradoException.porId(req.clienteId()));
        if (!Boolean.TRUE.equals(cliente.getActivo()))
            throw ValidacionNegocioException.clienteInactivo();
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

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaResponse> consultar(CuentaConsultaRequest req) {
        Specification<Cuenta> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getNumeroCuenta() != null && !req.getNumeroCuenta().isBlank())
                ps.add(cb.equal(root.get("numeroCuenta"), req.getNumeroCuenta()));
            if (req.getClienteId() != null)
                ps.add(cb.equal(root.get("cliente").get("id"), req.getClienteId()));
            if (req.getEstatus() != null)
                ps.add(cb.equal(root.get("estatus"), req.getEstatus()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return cuentaRepository.findAll(spec, paginacionFactory.toPageable(req.getPaginacion()))
                .map(mapper::aCuentaResponse);
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(CuentaUpdateRequest req) {
        Cuenta ct = cuentaRepository.findByNumeroCuenta(req.getNumeroCuenta())
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(req.getNumeroCuenta()));
        if (req.getEstatus() == EstatusCuenta.ACTIVA
                && !Boolean.TRUE.equals(ct.getCliente().getActivo())) {
            throw ValidacionNegocioException.clienteInactivo();
        }
        ct.setEstatus(req.getEstatus());
        return mapper.aCuentaResponse(cuentaRepository.save(ct));
    }
}
