package com.proyecto.servicios.onboarding.controller;

import com.proyecto.servicios.onboarding.dto.request.CuentaConsultaRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaCreateRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.CuentaResponse;
import com.proyecto.servicios.onboarding.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Cuentas bancarias asociadas a clientes")
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping("/crear")
    @Operation(summary = "Crea una cuenta adicional para un cliente existente activo")
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.crear(req));
    }

    @PostMapping("/consultar")
    @Operation(summary = "Consulta cuentas combinando filtros (numeroCuenta, clienteId, estatus). Devuelve saldo tambien. Paginada.")
    public ResponseEntity<Page<CuentaResponse>> consultar(@Valid @RequestBody CuentaConsultaRequest req) {
        return ResponseEntity.ok(cuentaService.consultar(req));
    }

    @PostMapping("/actualizar")
    @Operation(summary = "Actualiza el estatus de una cuenta. numeroCuenta y saldo son INMUTABLES.")
    public ResponseEntity<CuentaResponse> actualizar(@Valid @RequestBody CuentaUpdateRequest req) {
        return ResponseEntity.ok(cuentaService.actualizarParcial(req));
    }
}
