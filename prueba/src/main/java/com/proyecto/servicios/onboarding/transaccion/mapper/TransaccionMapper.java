package com.proyecto.servicios.onboarding.transaccion.mapper;

import com.proyecto.servicios.onboarding.transaccion.dto.response.TransaccionResponse;
import com.proyecto.servicios.onboarding.transaccion.entity.Transaccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransaccionMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "cuentaId",  source = "cuenta.id")
    TransaccionResponse toResponse(Transaccion t);
}
