package com.proyecto.servicios.onboarding.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CuentaCreateRequest(
        @NotNull(message = "El clienteId es obligatorio")
        Long clienteId,

        @DecimalMin(value = "0.00", inclusive = true, message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2)
        BigDecimal saldoInicial
) {}
