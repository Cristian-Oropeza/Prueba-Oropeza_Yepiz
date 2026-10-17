package com.proyecto.servicios.onboarding.service;

import com.proyecto.servicios.onboarding.dto.request.PaginacionRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Convierte PaginacionRequest a Pageable de Spring Data.
 */
@Component
public class PaginacionFactory {

    public Pageable toPageable(PaginacionRequest req) {
        if (req == null) req = new PaginacionRequest();
        Sort sort = parseSort(req.getSort());
        return PageRequest.of(req.getPage(), req.getSize(), sort);
    }

    private Sort parseSort(String raw) {
        if (raw == null || raw.isBlank()) return Sort.by(Sort.Direction.DESC, "fechaCreacion");
        String[] parts = raw.split(",");
        String campo = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, campo);
    }
}
