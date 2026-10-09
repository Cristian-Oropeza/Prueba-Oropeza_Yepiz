package com.proyecto.servicios.onboarding.repository;

import com.proyecto.servicios.onboarding.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByClienteId(Long clienteId);
    boolean existsByCorreo(String correo);

    Page<Usuario> findByCorreoContainingIgnoreCase(String correo, Pageable pageable);
    Page<Usuario> findByActivo(Boolean activo, Pageable pageable);
}
