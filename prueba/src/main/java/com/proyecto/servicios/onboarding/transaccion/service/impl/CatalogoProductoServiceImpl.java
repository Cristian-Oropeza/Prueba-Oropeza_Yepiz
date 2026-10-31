package com.proyecto.servicios.onboarding.transaccion.service.impl;

import com.proyecto.servicios.onboarding.transaccion.entity.CatalogoProducto;
import com.proyecto.servicios.onboarding.transaccion.exception.ProductoNoEncontradoException;
import com.proyecto.servicios.onboarding.transaccion.repository.CatalogoProductoRepository;
import com.proyecto.servicios.onboarding.transaccion.service.CatalogoProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogoProductoServiceImpl implements CatalogoProductoService {

    private final CatalogoProductoRepository repo;

    @Override
    @Transactional(readOnly = true)
    public CatalogoProducto obtenerActivoOExcepcion(String idServicio, String idProducto) {
        CatalogoProducto p = repo.findByIdServicioAndIdProducto(idServicio, idProducto)
                .orElseThrow(() -> new ProductoNoEncontradoException(
                        "El servicio '" + idServicio + "' con producto '" + idProducto + "' no esta en el catalogo"));
        if (!Boolean.TRUE.equals(p.getActivo())) {
            throw new ProductoNoEncontradoException(
                    "El producto '" + idProducto + "' esta inactivo");
        }
        return p;
    }
}
