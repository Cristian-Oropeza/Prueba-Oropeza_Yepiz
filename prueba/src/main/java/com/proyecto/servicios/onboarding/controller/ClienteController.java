package com.proyecto.servicios.onboarding.controller;

import com.proyecto.servicios.onboarding.dto.request.ClienteAltaRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.ClienteAltaResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResumenResponse;
import com.proyecto.servicios.onboarding.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Clientes", description = "Registro, consulta y actualizacion de clientes personas fisicas")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Registra cliente + domicilio + cuenta + usuario (transaccional, atomico)")
    public ResponseEntity<ClienteAltaResponse> registrar(@Valid @RequestBody ClienteAltaRequest req) {
        ClienteAltaResponse resp = clienteService.registrar(req);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + resp.id())).body(resp);
    }

    @GetMapping
    @Operation(summary = "Lista/busca clientes. Precedencia: curp > rfc > correo > numeroCuenta > apellidoPaterno > apellidoMaterno > nombre > rango fechas > paginado")
    public ResponseEntity<?> buscar(
            @RequestParam(required = false) String curp,
            @RequestParam(required = false) String rfc,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String numeroCuenta,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellidoPaterno,
            @RequestParam(required = false) String apellidoMaterno,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {

        if (curp != null)            return ResponseEntity.ok(clienteService.obtenerPorCurp(curp));
        if (rfc != null)             return ResponseEntity.ok(clienteService.obtenerPorRfc(rfc));
        if (correo != null)          return ResponseEntity.ok(clienteService.obtenerPorCorreo(correo));
        if (numeroCuenta != null)    return ResponseEntity.ok(clienteService.obtenerPorNumeroCuenta(numeroCuenta));
        if (apellidoPaterno != null) return ResponseEntity.ok(clienteService.buscarPorApellidoPaterno(apellidoPaterno, pageable));
        if (apellidoMaterno != null) return ResponseEntity.ok(clienteService.buscarPorApellidoMaterno(apellidoMaterno, pageable));
        if (nombre != null)          return ResponseEntity.ok(clienteService.buscarPorNombre(nombre, pageable));
        if (fechaDesde != null && fechaHasta != null)
            return ResponseEntity.ok(clienteService.buscarPorRangoFechas(fechaDesde, fechaHasta, pageable));

        Page<ClienteResumenResponse> page = clienteService.listar(pageable, activo);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un cliente por id")
    public ResponseEntity<ClienteResponse> porId(@PathVariable @Positive(message = "El id debe ser positivo") Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizacion parcial. CURP, RFC y numero de cuenta son INMUTABLES.")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ClienteUpdateRequest req) {
        return ResponseEntity.ok(clienteService.actualizarParcial(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Baja logica: desactiva cliente, cuentas activas y usuario asociado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable @Positive Long id) {
        clienteService.darDeBaja(id);
    }
}
