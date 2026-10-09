package com.proyecto.servicios.onboarding.mapper;

import com.proyecto.servicios.onboarding.dto.request.ClienteAltaRequest;
import com.proyecto.servicios.onboarding.dto.request.DomicilioRequest;
import com.proyecto.servicios.onboarding.dto.response.*;
import com.proyecto.servicios.onboarding.entity.Cliente;
import com.proyecto.servicios.onboarding.entity.Domicilio;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClienteMapper {

    public Cliente aEntidad(ClienteAltaRequest r) {
        Cliente c = Cliente.builder()
                .nombre(trim(r.nombre()))
                .segundoNombre(trim(r.segundoNombre()))
                .apellidoPaterno(trim(r.apellidoPaterno()))
                .apellidoMaterno(trim(r.apellidoMaterno()))
                .fechaNacimiento(r.fechaNacimiento())
                .curp(r.curp().toUpperCase())
                .rfc(r.rfc().toUpperCase())
                .sexo(r.sexo())
                .nacionalidad(trim(r.nacionalidad()))
                .estadoCivil(r.estadoCivil())
                .correo(r.correo().toLowerCase().trim())
                .telefonoMovil(r.telefonoMovil())
                .telefonoAlterno(r.telefonoAlterno())
                .ocupacion(trim(r.ocupacion()))
                .empresa(trim(r.empresa()))
                .ingresoMensual(r.ingresoMensual())
                .activo(Boolean.TRUE)
                .build();
        c.setDomicilio(aDomicilio(r.domicilio()));
        return c;
    }

    public Domicilio aDomicilio(DomicilioRequest d) {
        return Domicilio.builder()
                .calle(trim(d.calle()))
                .numeroExterior(trim(d.numeroExterior()))
                .numeroInterior(trim(d.numeroInterior()))
                .colonia(trim(d.colonia()))
                .municipio(trim(d.municipio()))
                .estado(trim(d.estado()))
                .codigoPostal(d.codigoPostal())
                .pais(trim(d.pais()))
                .build();
    }

    public ClienteResponse aResponse(Cliente c) {
        DomicilioResponse dom = c.getDomicilio() == null ? null : new DomicilioResponse(
                c.getDomicilio().getId(),
                c.getDomicilio().getCalle(),
                c.getDomicilio().getNumeroExterior(),
                c.getDomicilio().getNumeroInterior(),
                c.getDomicilio().getColonia(),
                c.getDomicilio().getMunicipio(),
                c.getDomicilio().getEstado(),
                c.getDomicilio().getCodigoPostal(),
                c.getDomicilio().getPais()
        );
        List<CuentaResponse> cuentas = c.getCuentas() == null ? List.of() :
                c.getCuentas().stream().map(this::aCuentaResponse).toList();
        return new ClienteResponse(
                c.getId(), c.getNombre(), c.getSegundoNombre(),
                c.getApellidoPaterno(), c.getApellidoMaterno(),
                c.getNombreCompleto(),
                c.getFechaNacimiento(),
                c.getCurp(), c.getRfc(),
                c.getSexo(), c.getNacionalidad(), c.getEstadoCivil(),
                c.getCorreo(), c.getTelefonoMovil(), c.getTelefonoAlterno(),
                c.getOcupacion(), c.getEmpresa(), c.getIngresoMensual(),
                c.getActivo(),
                c.getFechaCreacion(), c.getFechaActualizacion(),
                dom, cuentas
        );
    }

    public ClienteResumenResponse aResumen(Cliente c) {
        return new ClienteResumenResponse(
                c.getId(), c.getNombreCompleto(), c.getCurp(), c.getRfc(),
                c.getCorreo(), c.getActivo(), c.getFechaCreacion()
        );
    }

    public CuentaResponse aCuentaResponse(com.proyecto.servicios.onboarding.entity.Cuenta ct) {
        return new CuentaResponse(
                ct.getId(), ct.getNumeroCuenta(), ct.getCliente().getId(),
                ct.getSaldo(), ct.getEstatus(), ct.getFechaApertura()
        );
    }

    private String trim(String s) { return s == null ? null : s.trim(); }
}
