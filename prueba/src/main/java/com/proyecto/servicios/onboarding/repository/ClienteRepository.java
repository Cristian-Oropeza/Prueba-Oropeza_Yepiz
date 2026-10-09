package com.proyecto.servicios.onboarding.repository;

import com.proyecto.servicios.onboarding.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCurp(String curp);
    Optional<Cliente> findByRfc(String rfc);
    Optional<Cliente> findByCorreo(String correo);

    boolean existsByCurp(String curp);
    boolean existsByRfc(String rfc);
    boolean existsByCorreo(String correo);

    Page<Cliente> findByActivo(Boolean activo, Pageable pageable);

    @Query("""
           SELECT c FROM Cliente c
            WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))
           """)
    Page<Cliente> buscarPorNombre(@Param("nombre") String nombre, Pageable pageable);

    @Query("""
           SELECT c FROM Cliente c
            WHERE LOWER(c.apellidoPaterno) LIKE LOWER(CONCAT('%', :ap, '%'))
           """)
    Page<Cliente> buscarPorApellidoPaterno(@Param("ap") String ap, Pageable pageable);

    @Query("""
           SELECT c FROM Cliente c
            WHERE LOWER(c.apellidoMaterno) LIKE LOWER(CONCAT('%', :am, '%'))
           """)
    Page<Cliente> buscarPorApellidoMaterno(@Param("am") String am, Pageable pageable);

    Page<Cliente> findByFechaCreacionBetween(OffsetDateTime desde, OffsetDateTime hasta, Pageable pageable);
}
