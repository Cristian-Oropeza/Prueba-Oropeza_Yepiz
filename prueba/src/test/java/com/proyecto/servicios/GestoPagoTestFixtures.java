package com.proyecto.servicios;

import com.proyecto.servicios.model.gestopago.product.GestoPagoProduct;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;

public final class GestoPagoTestFixtures {

    private GestoPagoTestFixtures() {
    }

    public static byte[] readResource(String path) {
        try (InputStream stream = GestoPagoTestFixtures.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Recurso de prueba no encontrado: " + path);
            }
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static GestoPagoProduct product(Integer idProducto, String servicio, String producto, String precio) {
        GestoPagoProduct product = new GestoPagoProduct();
        product.setIdProducto(idProducto);
        product.setServicio(servicio);
        product.setProducto(producto);
        product.setPrecio(new BigDecimal(precio));
        return product;
    }
}
