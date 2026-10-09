package com.proyecto.servicios.onboarding.service.impl;

import com.proyecto.servicios.onboarding.dto.request.ClienteAltaRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteUpdateRequest;
import com.proyecto.servicios.onboarding.dto.request.DomicilioRequest;
import com.proyecto.servicios.onboarding.dto.response.*;
import com.proyecto.servicios.onboarding.entity.*;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import com.proyecto.servicios.onboarding.exception.*;
import com.proyecto.servicios.onboarding.mapper.ClienteMapper;
import com.proyecto.servicios.onboarding.repository.ClienteRepository;
import com.proyecto.servicios.onboarding.repository.CuentaRepository;
import com.proyecto.servicios.onboarding.repository.UsuarioRepository;
import com.proyecto.servicios.onboarding.service.ClienteService;
import com.proyecto.servicios.onboarding.service.NumeroCuentaGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper mapper;
    private final NumeroCuentaGenerator cuentaGen;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cuenta.saldo-inicial:0.00}")
    private BigDecimal saldoInicial;

    // --------- ALTA ---------
    @Override
    @Transactional
    public ClienteAltaResponse registrar(ClienteAltaRequest req) {
        // Pre-checks para dar errores claros antes de tocar constraints
        validarEdad(req.fechaNacimiento());
        String correo = req.correo().toLowerCase().trim();
        String curp   = req.curp().toUpperCase();
        String rfc    = req.rfc().toUpperCase();
        if (clienteRepository.existsByCurp(curp))     throw DuplicadoException.curp(curp);
        if (clienteRepository.existsByRfc(rfc))       throw DuplicadoException.rfc(rfc);
        if (clienteRepository.existsByCorreo(correo)) throw DuplicadoException.correo(correo);
        if (usuarioRepository.existsByCorreo(correo)) throw DuplicadoException.correo(correo);

        // 1. Cliente + Domicilio
        Cliente cliente = mapper.aEntidad(req);

        // 2. Cuenta inicial
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta(cuentaGen.generar())
                .saldo(saldoInicial)
                .estatus(EstatusCuenta.ACTIVA)
                .build();
        cliente.addCuenta(cuenta);

        // 3. Usuario
        Usuario usuario = Usuario.builder()
                .correo(correo)
                .password(passwordEncoder.encode(req.password()))
                .activo(Boolean.TRUE)
                .build();
        cliente.setUsuario(usuario);

        Cliente saved = clienteRepository.save(cliente);
        Cuenta cuentaCreada = saved.getCuentas().get(0);

        log.info("Cliente registrado id={} correo={} cuenta={}",
                saved.getId(), saved.getCorreo(), cuentaCreada.getNumeroCuenta());

        return new ClienteAltaResponse(
                saved.getId(), saved.getNombreCompleto(), saved.getCorreo(), saved.getActivo(),
                mapper.aCuentaResponse(cuentaCreada), Boolean.TRUE, saved.getFechaCreacion()
        );
    }

    // --------- CONSULTAS ---------
    @Override @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Long id) {
        return mapper.aResponse(clienteRepository.findById(id)
                .orElseThrow(() -> ClienteNoEncontradoException.porId(id)));
    }

    @Override @Transactional(readOnly = true)
    public ClienteResponse obtenerPorCurp(String curp) {
        return mapper.aResponse(clienteRepository.findByCurp(curp.toUpperCase())
                .orElseThrow(() -> ClienteNoEncontradoException.porCurp(curp)));
    }

    @Override @Transactional(readOnly = true)
    public ClienteResponse obtenerPorRfc(String rfc) {
        return mapper.aResponse(clienteRepository.findByRfc(rfc.toUpperCase())
                .orElseThrow(() -> ClienteNoEncontradoException.porRfc(rfc)));
    }

    @Override @Transactional(readOnly = true)
    public ClienteResponse obtenerPorCorreo(String correo) {
        return mapper.aResponse(clienteRepository.findByCorreo(correo.toLowerCase().trim())
                .orElseThrow(() -> ClienteNoEncontradoException.porCorreo(correo)));
    }

    @Override @Transactional(readOnly = true)
    public ClienteResponse obtenerPorNumeroCuenta(String numeroCuenta) {
        Cuenta ct = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(numeroCuenta));
        return mapper.aResponse(ct.getCliente());
    }

    @Override @Transactional(readOnly = true)
    public Page<ClienteResumenResponse> listar(Pageable pageable, Boolean activo) {
        Page<Cliente> page = (activo == null)
                ? clienteRepository.findAll(pageable)
                : clienteRepository.findByActivo(activo, pageable);
        return page.map(mapper::aResumen);
    }

    @Override @Transactional(readOnly = true)
    public Page<ClienteResumenResponse> buscarPorNombre(String nombre, Pageable p) {
        return clienteRepository.buscarPorNombre(nombre, p).map(mapper::aResumen);
    }

    @Override @Transactional(readOnly = true)
    public Page<ClienteResumenResponse> buscarPorApellidoPaterno(String ap, Pageable p) {
        return clienteRepository.buscarPorApellidoPaterno(ap, p).map(mapper::aResumen);
    }

    @Override @Transactional(readOnly = true)
    public Page<ClienteResumenResponse> buscarPorApellidoMaterno(String am, Pageable p) {
        return clienteRepository.buscarPorApellidoMaterno(am, p).map(mapper::aResumen);
    }

    @Override @Transactional(readOnly = true)
    public Page<ClienteResumenResponse> buscarPorRangoFechas(LocalDate desde, LocalDate hasta, Pageable p) {
        OffsetDateTime d = desde.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime h = hasta.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return clienteRepository.findByFechaCreacionBetween(d, h, p).map(mapper::aResumen);
    }

    // --------- PATCH ---------
    @Override
    @Transactional
    public ClienteResponse actualizarParcial(Long id, ClienteUpdateRequest r) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> ClienteNoEncontradoException.porId(id));

        // Correo: verificar unicidad si cambia
        if (r.correo() != null) {
            String nuevo = r.correo().toLowerCase().trim();
            if (!nuevo.equals(c.getCorreo())) {
                if (clienteRepository.existsByCorreo(nuevo) || usuarioRepository.existsByCorreo(nuevo)) {
                    throw DuplicadoException.correo(nuevo);
                }
                c.setCorreo(nuevo);
                // El usuario asociado tambien cambia su correo (fuente unica de verdad)
                if (c.getUsuario() != null) c.getUsuario().setCorreo(nuevo);
            }
        }
        if (r.nombre() != null)           c.setNombre(r.nombre().trim());
        if (r.segundoNombre() != null)    c.setSegundoNombre(r.segundoNombre().trim());
        if (r.apellidoPaterno() != null)  c.setApellidoPaterno(r.apellidoPaterno().trim());
        if (r.apellidoMaterno() != null)  c.setApellidoMaterno(r.apellidoMaterno().trim());
        if (r.sexo() != null)             c.setSexo(r.sexo());
        if (r.nacionalidad() != null)     c.setNacionalidad(r.nacionalidad().trim());
        if (r.estadoCivil() != null)      c.setEstadoCivil(r.estadoCivil());
        if (r.telefonoMovil() != null)    c.setTelefonoMovil(r.telefonoMovil());
        if (r.telefonoAlterno() != null)  c.setTelefonoAlterno(r.telefonoAlterno());
        if (r.ocupacion() != null)        c.setOcupacion(r.ocupacion().trim());
        if (r.empresa() != null)          c.setEmpresa(r.empresa().trim());
        if (r.ingresoMensual() != null)   c.setIngresoMensual(r.ingresoMensual());

        if (r.domicilio() != null) actualizarDomicilio(c.getDomicilio(), r.domicilio());

        return mapper.aResponse(clienteRepository.save(c));
    }

    private void actualizarDomicilio(Domicilio d, DomicilioRequest r) {
        if (d == null) return;
        if (r.calle() != null)          d.setCalle(r.calle().trim());
        if (r.numeroExterior() != null) d.setNumeroExterior(r.numeroExterior().trim());
        if (r.numeroInterior() != null) d.setNumeroInterior(r.numeroInterior().trim());
        if (r.colonia() != null)        d.setColonia(r.colonia().trim());
        if (r.municipio() != null)      d.setMunicipio(r.municipio().trim());
        if (r.estado() != null)         d.setEstado(r.estado().trim());
        if (r.codigoPostal() != null)   d.setCodigoPostal(r.codigoPostal());
        if (r.pais() != null)           d.setPais(r.pais().trim());
    }

    // --------- BAJA LOGICA ---------
    @Override
    @Transactional
    public void darDeBaja(Long id) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> ClienteNoEncontradoException.porId(id));
        c.setActivo(Boolean.FALSE);
        // Cuentas ACTIVAS -> INACTIVAS
        c.getCuentas().stream()
                .filter(ct -> ct.getEstatus() == EstatusCuenta.ACTIVA)
                .forEach(ct -> ct.setEstatus(EstatusCuenta.INACTIVA));
        // Usuario -> inactivo
        if (c.getUsuario() != null) c.getUsuario().setActivo(Boolean.FALSE);
        clienteRepository.save(c);
        log.info("Cliente id={} dado de baja logica", id);
    }

    // --------- Helpers ---------
    private void validarEdad(LocalDate f) {
        if (f == null || f.isAfter(LocalDate.now())
                || Period.between(f, LocalDate.now()).getYears() < 18) {
            throw ValidacionNegocioException.edadInvalida();
        }
    }
}
