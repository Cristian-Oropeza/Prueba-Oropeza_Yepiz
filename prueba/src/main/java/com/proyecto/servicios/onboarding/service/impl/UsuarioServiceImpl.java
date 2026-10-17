package com.proyecto.servicios.onboarding.service.impl;

import com.proyecto.servicios.onboarding.dto.request.UsuarioActualizarRequest;
import com.proyecto.servicios.onboarding.dto.request.UsuarioConsultaRequest;
import com.proyecto.servicios.onboarding.dto.response.UsuarioResponse;
import com.proyecto.servicios.onboarding.entity.Usuario;
import com.proyecto.servicios.onboarding.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.onboarding.mapper.UsuarioMapper;
import com.proyecto.servicios.onboarding.repository.UsuarioRepository;
import com.proyecto.servicios.onboarding.service.PaginacionFactory;
import com.proyecto.servicios.onboarding.service.UsuarioService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;
    private final PaginacionFactory paginacionFactory;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> consultar(UsuarioConsultaRequest req) {
        Specification<Usuario> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (req.getCorreo() != null && !req.getCorreo().isBlank())
                ps.add(cb.like(cb.lower(root.get("correo")),
                        "%" + req.getCorreo().toLowerCase() + "%"));
            if (req.getActivo() != null)
                ps.add(cb.equal(root.get("activo"), req.getActivo()));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return usuarioRepository.findAll(spec, paginacionFactory.toPageable(req.getPaginacion()))
                .map(mapper::aResponse);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarEstatus(UsuarioActualizarRequest req) {
        Usuario u = usuarioRepository.findByClienteId(req.clienteId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado para cliente " + req.clienteId()));
        u.setActivo(req.activo());
        return mapper.aResponse(usuarioRepository.save(u));
    }
}
