package com.proyecto.servicios.client;

import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class GestoPagoProductErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        GestoPagoErrorCode errorCode = resolveErrorCode(response.status());
        return new GestoPagoIntegrationException(errorCode, "HTTP " + response.status());
    }

    private GestoPagoErrorCode resolveErrorCode(int status) {
        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            return GestoPagoErrorCode.AUTHENTICATION_ERROR;
        }
        if (HttpStatusCode.valueOf(status).is5xxServerError()) {
            return GestoPagoErrorCode.SERVICE_UNAVAILABLE;
        }
        return GestoPagoErrorCode.CLIENT_ERROR;
    }
}