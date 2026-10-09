package com.proyecto.servicios.onboarding.controller;

import com.proyecto.servicios.onboarding.dto.request.UsuarioActualizarRequest;
import com.proyecto.servicios.onboarding.dto.response.UsuarioResponse;
import com.proyecto.servicios.onboarding.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Usuarios de acceso asociados a los clientes")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/filtro")
    @Operation(summary = "Filtra usuarios por correo o estatus")
    public ResponseEntity<Page<UsuarioResponse>> filtrar(
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.filtrar(correo, activo, pageable));
    }

    @PutMapping("/agregar")
    @Operation(summary = "Actualiza el estatus (activo/inactivo) del usuario asociado a un cliente")
    public ResponseEntity<UsuarioResponse> actualizar(@Valid @RequestBody UsuarioActualizarRequest req) {
        return ResponseEntity.ok(usuarioService.actualizarEstatus(req.clienteId(), req.activo()));
    }
}
