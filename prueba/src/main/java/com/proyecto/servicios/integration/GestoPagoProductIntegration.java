package com.proyecto.servicios.integration;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.model.gestopago.product.GestoPagoMessage;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProduct;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProductListResponse;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.List;

@Slf4j
@Component
public class GestoPagoProductIntegration {

    private static final String OPERATION = "GestoPago getProductList";

    private final GestoPagoProductClient productClient;
    private final GestoPagoProductXmlParser xmlParser;

    public GestoPagoProductIntegration(GestoPagoProductClient productClient,
                                       GestoPagoProductXmlParser xmlParser) {
        this.productClient = productClient;
        this.xmlParser = xmlParser;
    }

    public List<GestoPagoProduct> obtenerProductos() {
        log.info("Inicio invocacion {}", OPERATION);
        long inicio = System.currentTimeMillis();
        try {
            GestoPagoProductListResponse response = xmlParser.parse(invocarServicio());
            validarMensaje(response.getMensaje());
            log.info("Fin invocacion {}: {} productos en {} ms",
                    OPERATION, response.getProductos().size(), duracionDesde(inicio));
            return response.getProductos();
        } catch (GestoPagoIntegrationException e) {
            log.error("Fin invocacion {} con error: codigo={}, detalle={}, duracion={} ms",
                    OPERATION, e.getErrorCode().getCode(), e.getMessage(), duracionDesde(inicio));
            throw e;
        }
    }

    private byte[] invocarServicio() {
        try {
            return productClient.getProductList();
        } catch (RetryableException e) {
            throw new GestoPagoIntegrationException(resolverErrorDeConexion(e), e);
        } catch (FeignException e) {
            throw new GestoPagoIntegrationException(GestoPagoErrorCode.COMMUNICATION_ERROR, e);
        }
    }

    private GestoPagoErrorCode resolverErrorDeConexion(RetryableException e) {
        boolean esTimeout = ExceptionUtils.indexOfType(e, SocketTimeoutException.class) >= 0;
        return esTimeout ? GestoPagoErrorCode.TIMEOUT : GestoPagoErrorCode.COMMUNICATION_ERROR;
    }

    private void validarMensaje(GestoPagoMessage mensaje) {
        if (mensaje == null) {
            throw new GestoPagoIntegrationException(GestoPagoErrorCode.INVALID_RESPONSE, "respuesta sin MENSAJE");
        }
        if (!mensaje.isExitoso()) {
            throw new GestoPagoIntegrationException(GestoPagoErrorCode.BUSINESS_ERROR,
                    "codigo=" + mensaje.getCodigo() + ", texto=" + mensaje.getTexto());
        }
    }

    private long duracionDesde(long inicio) {
        return System.currentTimeMillis() - inicio;
    }
}