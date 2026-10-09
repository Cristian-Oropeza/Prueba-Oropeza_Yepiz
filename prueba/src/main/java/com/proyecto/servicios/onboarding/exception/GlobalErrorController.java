package com.proyecto.servicios.onboarding.exception;

import com.proyecto.servicios.onboarding.dto.response.GenericResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller global de errores del container (Tomcat).
 *
 * Captura CUALQUIER error que llegue a Spring por la ruta /error:
 *  - 404 cuando no hay handler (p. ej. ruta inexistente /foo/bar)
 *  - 400 cuando Tomcat rechaza por "Request header too large", URL mal formada, etc.
 *  - 500 cuando una excepcion no fue atrapada por el GlobalExceptionHandler
 *
 * Devuelve siempre JSON con el contrato uniforme {@link GenericResponse},
 * nunca el HTML blanco de Tomcat.
 */
@RestController
@Slf4j
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<GenericResponse> handleError(HttpServletRequest request) {
        int status = resolveStatus(request);
        String mensaje = resolveMessage(request, status);
        log.warn("Error de container: status={} path={} msg={}",
                status, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI), mensaje);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(GenericResponse.of(status, mensaje));
    }

    private int resolveStatus(HttpServletRequest req) {
        Object s = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (s instanceof Integer i && i > 0) return i;
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private String resolveMessage(HttpServletRequest req, int status) {
        Object m = req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (m instanceof String s && !s.isBlank()) return s;
        return switch (status) {
            case 400 -> "Peticion invalida";
            case 404 -> "Recurso no encontrado";
            case 405 -> "Metodo HTTP no permitido para este recurso";
            case 413 -> "El tamanio de la peticion excede el permitido";
            case 414 -> "La URI es demasiado larga";
            case 415 -> "Content-Type no soportado";
            case 500 -> "Error interno del servidor";
            default  -> HttpStatus.valueOf(status).getReasonPhrase();
        };
    }
}
