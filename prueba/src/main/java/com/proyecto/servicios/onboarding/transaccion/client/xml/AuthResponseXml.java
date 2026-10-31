package com.proyecto.servicios.onboarding.transaccion.client.xml;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Respuesta del endpoint de autenticacion de GestoPago (puede ser JSON o XML segun ambiente).
 * Normalizamos aqui los campos tipicos.
 */
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponseXml {

    @JsonAlias({"token", "access_token", "TOKEN"})
    private String token;

    @JsonAlias({"expiresIn", "expires_in", "VIGENCIA_HORAS", "vigenciaHoras"})
    private Long vigenciaHoras;
}
