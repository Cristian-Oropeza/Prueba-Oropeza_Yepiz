package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.document.sf.Personas;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonasRepository extends MongoRepository<Personas, String> {

    Optional<Personas> findByNombre(String nombre);
}
