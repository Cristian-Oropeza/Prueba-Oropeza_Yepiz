package com.proyecto.servicios.onboarding.controller;

import com.proyecto.servicios.onboarding.dto.request.UsuarioActualizarRequest;
import com.proyecto.servicios.onboarding.dto.request.UsuarioConsultaRequest;
import com.proyecto.servicios.onboarding.dto.response.UsuarioResponse;
import com.proyecto.servicios.onboarding.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Usuarios de acceso asociados a los clientes")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/consultar")
    @Operation(summary = "Consulta usuarios combinando filtros (correo, activo). Paginada.")
    public ResponseEntity<Page<UsuarioResponse>> consultar(@Valid @RequestBody UsuarioConsultaRequest req) {
        return ResponseEntity.ok(usuarioService.consultar(req));
    }

    @PostMapping("/actualizar")
    @Operation(summary = "Activa o desactiva el usuario de un cliente")
    public ResponseEntity<UsuarioResponse> actualizar(@Valid @RequestBody UsuarioActualizarRequest req) {
        return ResponseEntity.ok(usuarioService.actualizarEstatus(req));
    }
}
