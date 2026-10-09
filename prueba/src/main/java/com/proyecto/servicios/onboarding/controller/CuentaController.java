package com.proyecto.servicios.onboarding.controller;

import com.proyecto.servicios.onboarding.dto.request.CuentaCreateRequest;
import com.proyecto.servicios.onboarding.dto.request.CuentaUpdateRequest;
import com.proyecto.servicios.onboarding.dto.response.CuentaResponse;
import com.proyecto.servicios.onboarding.dto.response.CuentaSaldoResponse;
import com.proyecto.servicios.onboarding.entity.enums.EstatusCuenta;
import com.proyecto.servicios.onboarding.service.CuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
@Validated
@Tag(name = "Cuentas", description = "Cuentas bancarias asociadas a clientes")
public class CuentaController {

    // Numero de cuenta: 18 digitos (CLABE)
    private static final String NUMERO_CUENTA_REGEX = "^\\d{18}$";
    private static final String NUMERO_CUENTA_MSG   = "El numero de cuenta debe tener 18 digitos";

    private final CuentaService cuentaService;

    @PostMapping
    @Operation(summary = "Crea una cuenta adicional para un cliente existente activo")
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaCreateRequest req) {
        CuentaResponse resp = cuentaService.crear(req);
        return ResponseEntity.created(URI.create("/api/v1/cuentas/" + resp.numeroCuenta())).body(resp);
    }

    @GetMapping("/{numeroCuenta}")
    @Operation(summary = "Consulta una cuenta por numero")
    public ResponseEntity<CuentaResponse> porNumero(
            @PathVariable @Pattern(regexp = NUMERO_CUENTA_REGEX, message = NUMERO_CUENTA_MSG) String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerPorNumero(numeroCuenta));
    }

    @GetMapping("/{numeroCuenta}/saldo")
    @Operation(summary = "Consulta el saldo de una cuenta")
    public ResponseEntity<CuentaSaldoResponse> saldo(
            @PathVariable @Pattern(regexp = NUMERO_CUENTA_REGEX, message = NUMERO_CUENTA_MSG) String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerSaldo(numeroCuenta));
    }

    @GetMapping
    @Operation(summary = "Consulta cuentas por clienteId o por estatus")
    public ResponseEntity<?> buscar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstatusCuenta estatus,
            @PageableDefault(size = 20, sort = "fechaApertura") Pageable pageable) {
        if (clienteId != null) {
            List<CuentaResponse> list = cuentaService.obtenerPorCliente(clienteId);
            return ResponseEntity.ok(list);
        }
        if (estatus != null) {
            Page<CuentaResponse> page = cuentaService.buscarPorEstatus(estatus, pageable);
            return ResponseEntity.ok(page);
        }
        return ResponseEntity.badRequest().body("Debe proveer 'clienteId' o 'estatus' como filtro");
    }

    @PatchMapping("/{numeroCuenta}")
    @Operation(summary = "Actualiza estatus. numero_cuenta y saldo son INMUTABLES.")
    public ResponseEntity<CuentaResponse> actualizar(
            @PathVariable @Pattern(regexp = NUMERO_CUENTA_REGEX, message = NUMERO_CUENTA_MSG) String numeroCuenta,
            @Valid @RequestBody CuentaUpdateRequest req) {
        return ResponseEntity.ok(cuentaService.actualizarParcial(numeroCuenta, req));
    }
}
