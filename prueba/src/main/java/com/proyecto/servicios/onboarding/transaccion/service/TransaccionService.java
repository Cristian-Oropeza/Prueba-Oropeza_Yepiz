package com.proyecto.servicios.onboarding.transaccion.service;

import com.proyecto.servicios.onboarding.transaccion.dto.request.TransaccionConsultaRequest;
import com.proyecto.servicios.onboarding.transaccion.dto.request.TransaccionEjecutarRequest;
import com.proyecto.servicios.onboarding.transaccion.dto.response.TransaccionResponse;

public interface TransaccionService {

    /** Ejecuta sendTx: valida, reserva saldo, envia a GestoPago y aplica/revierte segun resultado. */
    TransaccionResponse ejecutar(TransaccionEjecutarRequest req);

    /** Consulta el estado vivo de una transaccion por referencia_operacion (opaca, publica). */
    TransaccionResponse consultar(TransaccionConsultaRequest req);
}
