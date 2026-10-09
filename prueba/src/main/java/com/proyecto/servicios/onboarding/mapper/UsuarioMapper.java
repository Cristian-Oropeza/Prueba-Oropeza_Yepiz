package com.proyecto.servicios.onboarding.mapper;

import com.proyecto.servicios.onboarding.dto.response.UsuarioResponse;
import com.proyecto.servicios.onboarding.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    public UsuarioResponse aResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getCliente().getId(), u.getCorreo(),
                u.getActivo(), u.getFechaCreacion(), u.getFechaActualizacion()
        );
    }
}
