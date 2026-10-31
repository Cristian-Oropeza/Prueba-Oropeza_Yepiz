package com.proyecto.servicios.onboarding.transaccion.controller;

import com.proyecto.servicios.onboarding.transaccion.dto.request.TransaccionConsultaRequest;
import com.proyecto.servicios.onboarding.transaccion.dto.request.TransaccionEjecutarRequest;
import com.proyecto.servicios.onboarding.transaccion.dto.response.TransaccionResponse;
import com.proyecto.servicios.onboarding.transaccion.entity.enums.EstadoTransaccion;
import com.proyecto.servicios.onboarding.transaccion.service.TransaccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de transacciones GestoPago. Convencion del proyecto: todo POST + @RequestBody,
 * identificadores por body, cero @PathVariable y cero @RequestParam.
 */
@RestController
@RequestMapping("/api/v1/transacciones")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Pagos y recargas via GestoPago/PuntoRed. " +
        "Flujo: validar -> apartar saldo (optimista) -> enviar cifrado -> interpretar o conciliar.")
public class TransaccionController {

    private final TransaccionService service;

    @PostMapping
    @Operation(summary = "Ejecuta una transaccion contra GestoPago (sendTx)",
            description = "Valida cliente, cuenta, producto, reglas de tipoFront, limites y anti-duplicado. " +
                    "Aparta el saldo con bloqueo optimista antes de llamar al proveedor. " +
                    "Si llega una idempotencyKey previamente usada, retorna la transaccion previa sin cobrar de nuevo. " +
                    "Si el proveedor no responde a tiempo, la transaccion queda en EN_CONFIRMACION (HTTP 202) para ser conciliada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "APLICADA (NUM_AUTORIZACION > 0)"),
            @ApiResponse(responseCode = "202", description = "EN_CONFIRMACION (sin respuesta clara, se conciliara)"),
            @ApiResponse(responseCode = "400", description = "Request invalido"),
            @ApiResponse(responseCode = "403", description = "Cuenta no pertenece al cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente, cuenta o producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Idempotencia o duplicado sospechoso"),
            @ApiResponse(responseCode = "422", description = "Reglas de negocio (saldo, limite, monto, regla producto)"),
            @ApiResponse(responseCode = "503", description = "Modulo deshabilitado o cifrado no configurado")
    })
    public ResponseEntity<TransaccionResponse> ejecutar(
            @RequestBody @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(name = "Recarga Telcel $100", value = """
                            {
                              "clienteId": 1,
                              "idempotencyKey": "op-2026-10-30-abc123",
                              "numeroCuenta": "014180012345678901",
                              "idServicio": "TELCEL",
                              "idProducto": "TELCEL_100",
                              "monto": 100.00,
                              "telefono": "5512345678"
                            }""")))
            TransaccionEjecutarRequest req) {

        TransaccionResponse resp = service.ejecutar(req);
        HttpStatus status = switch (resp.getEstado()) {
            case APLICADA -> HttpStatus.OK;
            case EN_CONFIRMACION, PENDIENTE -> HttpStatus.ACCEPTED;
            case RECHAZADA -> HttpStatus.UNPROCESSABLE_ENTITY;
            case NO_ENVIADA -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        return ResponseEntity.status(status).body(resp);
    }

    @PostMapping("/consulta")
    @Operation(summary = "Consulta el estado vivo de una transaccion por referencia_operacion")
    public ResponseEntity<TransaccionResponse> consultar(@RequestBody @Valid TransaccionConsultaRequest req) {
        return ResponseEntity.ok(service.consultar(req));
    }
}
