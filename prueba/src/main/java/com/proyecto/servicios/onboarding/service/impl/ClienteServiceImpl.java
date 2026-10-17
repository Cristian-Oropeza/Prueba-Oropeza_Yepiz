package com.proyecto.servicios.onboarding.service.impl;

import com.proyecto.servicios.onboarding.dto.request.*;
import com.proyecto.servicios.onboarding.dto.response.*;
import com.proyecto.servicios.onboarding.entity.*;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import com.proyecto.servicios.onboarding.exception.*;
import com.proyecto.servicios.onboarding.mapper.ClienteMapper;
import com.proyecto.servicios.onboarding.repository.ClienteRepository;
import com.proyecto.servicios.onboarding.repository.UsuarioRepository;
import com.proyecto.servicios.onboarding.service.ClienteService;
import com.proyecto.servicios.onboarding.service.NumeroCuentaGenerator;
import com.proyecto.servicios.onboarding.service.PaginacionFactory;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper mapper;
    private final NumeroCuentaGenerator cuentaGen;
    private final PasswordEncoder passwordEncoder;
    private final PaginacionFactory paginacionFactory;

    @Value("${app.cuenta.saldo-inicial:0.00}")
    private BigDecimal saldoInicial;

    // ---------- ALTA ----------
    @Override
    @Transactional
    public ClienteAltaResponse registrar(ClienteAltaRequest req) {
        validarEdad(req.fechaNacimiento());
        String correo = req.correo().toLowerCase().trim();
        String curp   = req.curp().toUpperCase();
        String rfc    = req.rfc().toUpperCase();
        if (clienteRepository.existsByCurp(curp))     throw DuplicadoException.curp(curp);
        if (clienteRepository.existsByRfc(rfc))       throw DuplicadoException.rfc(rfc);
        if (clienteRepository.existsByCorreo(correo)) throw DuplicadoException.correo(correo);
        if (usuarioRepository.existsByCorreo(correo)) throw DuplicadoException.correo(correo);

        Cliente cliente = mapper.aEntidad(req);
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta(cuentaGen.generar())
                .saldo(saldoInicial)
                .estatus(EstatusCuenta.ACTIVA)
                .build();
        cliente.addCuenta(cuenta);
        Usuario usuario = Usuario.builder()
                .correo(correo)
                .password(passwordEncoder.encode(req.password()))
                .activo(Boolean.TRUE)
                .build();
        cliente.setUsuario(usuario);

        Cliente saved = clienteRepository.save(cliente);
        Cuenta cuentaCreada = saved.getCuentas().get(0);
        log.info("Cliente registrado id={} correo={}", saved.getId(), saved.getCorreo());

        return new ClienteAltaResponse(
                saved.getId(), saved.getNombreCompleto(), saved.getCorreo(), saved.getActivo(),
                mapper.aCuentaResponse(cuentaCreada), Boolean.TRUE, saved.getFechaCreacion()
        );
    }

    // ---------- CONSULTA UNIFICADA ----------
    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResumenResponse> consultar(ClienteConsultaRequest req) {
        Specification<Cliente> spec = construirSpec(req);
        return clienteRepository.findAll(spec, paginacionFactory.toPageable(req.getPaginacion()))
                .map(mapper::aResumen);
    }

    private Specification<Cliente> construirSpec(ClienteConsultaRequest req) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getId() != null)
                ps.add(cb.equal(root.get("id"), req.getId()));
            if (req.getCurp() != null && !req.getCurp().isBlank())
                ps.add(cb.equal(root.get("curp"), req.getCurp().toUpperCase()));
            if (req.getRfc() != null && !req.getRfc().isBlank())
                ps.add(cb.equal(root.get("rfc"), req.getRfc().toUpperCase()));
            if (req.getCorreo() != null && !req.getCorreo().isBlank())
                ps.add(cb.equal(cb.lower(root.get("correo")), req.getCorreo().toLowerCase().trim()));
            if (req.getNumeroCuenta() != null && !req.getNumeroCuenta().isBlank()) {
                Join<Cliente, Cuenta> cuentas = root.join("cuentas");
                ps.add(cb.equal(cuentas.get("numeroCuenta"), req.getNumeroCuenta()));
            }
            if (req.getNombre() != null && !req.getNombre().isBlank())
                ps.add(cb.like(cb.lower(root.get("nombre")), "%" + req.getNombre().toLowerCase() + "%"));
            if (req.getApellidoPaterno() != null && !req.getApellidoPaterno().isBlank())
                ps.add(cb.like(cb.lower(root.get("apellidoPaterno")), "%" + req.getApellidoPaterno().toLowerCase() + "%"));
            if (req.getApellidoMaterno() != null && !req.getApellidoMaterno().isBlank())
                ps.add(cb.like(cb.lower(root.get("apellidoMaterno")), "%" + req.getApellidoMaterno().toLowerCase() + "%"));
            if (req.getActivo() != null)
                ps.add(cb.equal(root.get("activo"), req.getActivo()));
            if (req.getFechaDesde() != null) {
                OffsetDateTime d = req.getFechaDesde().atStartOfDay().atOffset(ZoneOffset.UTC);
                ps.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), d));
            }
            if (req.getFechaHasta() != null) {
                OffsetDateTime h = req.getFechaHasta().plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                ps.add(cb.lessThan(root.get("fechaCreacion"), h));
            }
            if (query != null) query.distinct(true);
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    // ---------- PATCH ----------
    @Override
    @Transactional
    public ClienteResponse actualizarParcial(ClienteUpdateRequest r) {
        Cliente c = clienteRepository.findById(r.getId())
                .orElseThrow(() -> ClienteNoEncontradoException.porId(r.getId()));

        if (r.getCorreo() != null) {
            String nuevo = r.getCorreo().toLowerCase().trim();
            if (!nuevo.equals(c.getCorreo())) {
                if (clienteRepository.existsByCorreo(nuevo) || usuarioRepository.existsByCorreo(nuevo))
                    throw DuplicadoException.correo(nuevo);
                c.setCorreo(nuevo);
                if (c.getUsuario() != null) c.getUsuario().setCorreo(nuevo);
            }
        }
        if (r.getNombre() != null)          c.setNombre(r.getNombre().trim());
        if (r.getSegundoNombre() != null)   c.setSegundoNombre(r.getSegundoNombre().trim());
        if (r.getApellidoPaterno() != null) c.setApellidoPaterno(r.getApellidoPaterno().trim());
        if (r.getApellidoMaterno() != null) c.setApellidoMaterno(r.getApellidoMaterno().trim());
        if (r.getSexo() != null)            c.setSexo(r.getSexo());
        if (r.getNacionalidad() != null)    c.setNacionalidad(r.getNacionalidad().trim());
        if (r.getEstadoCivil() != null)     c.setEstadoCivil(r.getEstadoCivil());
        if (r.getTelefonoMovil() != null)   c.setTelefonoMovil(r.getTelefonoMovil());
        if (r.getTelefonoAlterno() != null) c.setTelefonoAlterno(r.getTelefonoAlterno());
        if (r.getOcupacion() != null)       c.setOcupacion(r.getOcupacion().trim());
        if (r.getEmpresa() != null)         c.setEmpresa(r.getEmpresa().trim());
        if (r.getIngresoMensual() != null)  c.setIngresoMensual(r.getIngresoMensual());

        if (r.getDomicilio() != null && c.getDomicilio() != null) {
            Domicilio d = c.getDomicilio();
            DomicilioRequest nd = r.getDomicilio();
            if (nd.calle() != null)          d.setCalle(nd.calle().trim());
            if (nd.numeroExterior() != null) d.setNumeroExterior(nd.numeroExterior().trim());
            if (nd.numeroInterior() != null) d.setNumeroInterior(nd.numeroInterior().trim());
            if (nd.colonia() != null)        d.setColonia(nd.colonia().trim());
            if (nd.municipio() != null)      d.setMunicipio(nd.municipio().trim());
            if (nd.estado() != null)         d.setEstado(nd.estado().trim());
            if (nd.codigoPostal() != null)   d.setCodigoPostal(nd.codigoPostal());
            if (nd.pais() != null)           d.setPais(nd.pais().trim());
        }
        return mapper.aResponse(clienteRepository.save(c));
    }

    // ---------- BAJA LOGICA ----------
    @Override
    @Transactional
    public void darDeBaja(ClienteIdRequest req) {
        Cliente c = clienteRepository.findById(req.getId())
                .orElseThrow(() -> ClienteNoEncontradoException.porId(req.getId()));
        c.setActivo(Boolean.FALSE);
        c.getCuentas().stream()
                .filter(ct -> ct.getEstatus() == EstatusCuenta.ACTIVA)
                .forEach(ct -> ct.setEstatus(EstatusCuenta.INACTIVA));
        if (c.getUsuario() != null) c.getUsuario().setActivo(Boolean.FALSE);
        clienteRepository.save(c);
        log.info("Cliente id={} dado de baja logica", req.getId());
    }

    private void validarEdad(LocalDate f) {
        if (f == null || f.isAfter(LocalDate.now())
                || Period.between(f, LocalDate.now()).getYears() < 18) {
            throw ValidacionNegocioException.edadInvalida();
        }
    }
}
