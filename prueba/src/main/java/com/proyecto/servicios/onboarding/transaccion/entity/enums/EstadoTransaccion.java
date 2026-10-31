package com.proyecto.servicios.onboarding.transaccion.entity.enums;

/**
 * Estados de la transaccion segun el documento de integracion de GestoPago.
 *   PENDIENTE       : saldo reservado, aun no enviada o en vuelo.
 *   EN_CONFIRMACION : enviada sin respuesta clara (timeout o codigo 82), espera conciliacion con confirmTx.
 *   APLICADA        : NUM_AUTORIZACION > 0 - cargo firme.
 *   RECHAZADA       : rechazada por el proveedor (-1 o codigo de error) - saldo devuelto.
 *   NO_ENVIADA      : ni siquiera salio al proveedor (ej. cifrado o token caidos) - saldo devuelto.
 */
public enum EstadoTransaccion {
    PENDIENTE,
    EN_CONFIRMACION,
    APLICADA,
    RECHAZADA,
    NO_ENVIADA
}
