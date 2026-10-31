package com.proyecto.servicios.onboarding.transaccion.repository;

import com.proyecto.servicios.onboarding.transaccion.entity.CatalogoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogoProductoRepository
        extends JpaRepository<CatalogoProducto, Long>,
                JpaSpecificationExecutor<CatalogoProducto> {

    Optional<CatalogoProducto> findByIdServicioAndIdProducto(String idServicio, String idProducto);
}
