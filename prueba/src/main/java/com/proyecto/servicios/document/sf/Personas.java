package com.proyecto.servicios.document.sf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "personas")
public class Personas {

    @Id
    private String id;

    private String nombre;

    private String apellidoP;

    private String apellidoMaterno;
}
