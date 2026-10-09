package com.proyecto.servicios.onboarding.entity;

import com.proyecto.servicios.onboarding.entity.enums.EstadoCivil;
import com.proyecto.servicios.onboarding.entity.enums.Sexo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_clientes_curp",   columnNames = "curp"),
                @UniqueConstraint(name = "uk_clientes_rfc",    columnNames = "rfc"),
                @UniqueConstraint(name = "uk_clientes_correo", columnNames = "correo")
        },
        indexes = {
                @Index(name = "idx_clientes_fecha_creacion", columnList = "fecha_creacion")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 50)
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 18)
    private String curp;

    @Column(nullable = false, length = 13)
    private String rfc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sexo sexo;

    @Column(nullable = false, length = 60)
    private String nacionalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 20)
    private EstadoCivil estadoCivil;

    @Column(nullable = false, length = 100)
    private String correo;

    @Column(name = "telefono_movil", nullable = false, length = 10)
    private String telefonoMovil;

    @Column(name = "telefono_alterno", length = 10)
    private String telefonoAlterno;

    @Column(nullable = false, length = 80)
    private String ocupacion;

    @Column(nullable = false, length = 100)
    private String empresa;

    @Column(name = "ingreso_mensual", nullable = false, precision = 12, scale = 2)
    private BigDecimal ingresoMensual;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = Boolean.TRUE;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    // Relaciones (LAZY para no arrastrar todo en cada find)
    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Domicilio domicilio;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Cuenta> cuentas = new ArrayList<>();

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Usuario usuario;

    // Helpers de asociacion bidireccional
    public void setDomicilio(Domicilio dom) {
        if (dom != null) dom.setCliente(this);
        this.domicilio = dom;
    }

    public void addCuenta(Cuenta cuenta) {
        cuenta.setCliente(this);
        this.cuentas.add(cuenta);
    }

    public void setUsuario(Usuario usuario) {
        if (usuario != null) usuario.setCliente(this);
        this.usuario = usuario;
    }

    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder(nombre);
        if (segundoNombre != null && !segundoNombre.isBlank()) sb.append(' ').append(segundoNombre);
        sb.append(' ').append(apellidoPaterno).append(' ').append(apellidoMaterno);
        return sb.toString();
    }
}
