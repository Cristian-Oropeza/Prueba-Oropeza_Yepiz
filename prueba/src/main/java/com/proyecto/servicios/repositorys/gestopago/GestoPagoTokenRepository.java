package com.proyecto.servicios.repositorys.gestopago;

import com.proyecto.servicios.document.gestopago.GestoPagoToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GestoPagoTokenRepository extends MongoRepository<GestoPagoToken, String> {

    Optional<GestoPagoToken> findByIdDistribuidorAndCodigoDispositivo(Integer idDistribuidor, String codigoDispositivo);
}
