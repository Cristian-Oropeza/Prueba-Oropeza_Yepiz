package com.proyecto.servicios.document.gestopago;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "gestopago_tokens")
@CompoundIndex(name = "uq_gestopago_tokens", def = "{'idDistribuidor': 1, 'codigoDispositivo': 1}", unique = true)
public class GestoPagoToken {

    @Id
    private String id;

    private Integer idDistribuidor;

    private String codigoDispositivo;

    private String token;

    private String tokenType;

    private Long expiresIn;

    @CreatedDate
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaActualizacion;

    private Boolean activo = true;
}
