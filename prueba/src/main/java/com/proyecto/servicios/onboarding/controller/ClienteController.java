package com.proyecto.servicios.onboarding.controller;

import com.proyecto.servicios.onboarding.dto.request.ClienteAltaRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteConsultaRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteIdRequest;
import com.proyecto.servicios.onboarding.dto.request.ClienteUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.ClienteAltaResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResponse;
import com.proyecto.servicios.onboarding.dto.response.ClienteResumenResponse;
import com.proyecto.servicios.onboarding.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Registro, consulta y actualizacion de clientes personas fisicas")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping("/registrar")
    @Operation(summary = "Registra cliente + domicilio + cuenta + usuario (transaccional)")
    public ResponseEntity<ClienteAltaResponse> registrar(@Valid @RequestBody ClienteAltaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrar(req));
    }

    @PostMapping("/consultar")
    @Operation(summary = "Consulta clientes combinando filtros (id, curp, rfc, correo, numeroCuenta, nombre, apellidos, activo, rango de fechas). Resultado paginado.")
    public ResponseEntity<Page<ClienteResumenResponse>> consultar(@Valid @RequestBody ClienteConsultaRequest req) {
        return ResponseEntity.ok(clienteService.consultar(req));
    }

    @PostMapping("/actualizar")
    @Operation(summary = "Actualizacion parcial. CURP, RFC y numero de cuenta son INMUTABLES.")
    public ResponseEntity<ClienteResponse> actualizar(@Valid @RequestBody ClienteUpdateRequest req) {
        return ResponseEntity.ok(clienteService.actualizarParcial(req));
    }

    @PostMapping("/eliminar")
    @Operation(summary = "Baja logica: desactiva cliente, cuentas activas y usuario asociado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Valid @RequestBody ClienteIdRequest req) {
        clienteService.darDeBaja(req);
    }
}
