package com.proyecto.servicios.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "domicilios",
        uniqueConstraints = @UniqueConstraint(name = "uk_domicilios_cliente", columnNames = "cliente_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_domicilios_cliente"))
    private Cliente cliente;

    @Column(nullable = false, length = 120)
    private String calle;

    @Column(name = "numero_exterior", nullable = false, length = 10)
    private String numeroExterior;

    @Column(name = "numero_interior", length = 10)
    private String numeroInterior;

    @Column(nullable = false, length = 80)
    private String colonia;

    @Column(nullable = false, length = 80)
    private String municipio;

    @Column(nullable = false, length = 60)
    private String estado;

    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;

    @Column(nullable = false, length = 60)
    @Builder.Default
    private String pais = "México";
}
