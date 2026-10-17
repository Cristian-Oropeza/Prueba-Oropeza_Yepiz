package com.proyecto.servicios.onboarding.repository;

import com.proyecto.servicios.onboarding.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByClienteId(Long clienteId);
    boolean existsByCorreo(String correo);
}
