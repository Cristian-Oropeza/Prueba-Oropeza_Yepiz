package com.proyecto.servicios.repositorys.mongo;

import com.proyecto.servicios.document.gestopago.ProductoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductoRepository extends MongoRepository<ProductoDocument, Integer> {

    List<ProductoDocument> findAllByOrderByServicioAscProductoAsc();

    List<ProductoDocument> findByServicioIgnoreCaseOrderByProductoAsc(String servicio);

    long deleteByFechaSincronizacionBefore(LocalDateTime fechaSincronizacion);
}
