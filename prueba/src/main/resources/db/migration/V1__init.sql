-- ============================================================================
-- Onboarding Clientes Personas Fisicas - Esquema inicial
-- ============================================================================
-- Convenciones:
--   * Nombres en snake_case, singular las columnas, plural las tablas.
--   * Timestamps en UTC (TIMESTAMPTZ).
--   * Baja logica en clientes/cuentas/usuarios mediante flag "activo".
--   * Los CHECK garantizan invariantes basicas incluso si se toca la BD directo.
-- ============================================================================

-- Tabla: clientes -------------------------------------------------------------
CREATE TABLE clientes (
    id                    BIGSERIAL       PRIMARY KEY,
    nombre                VARCHAR(50)     NOT NULL,
    segundo_nombre        VARCHAR(50),
    apellido_paterno      VARCHAR(50)     NOT NULL,
    apellido_materno      VARCHAR(50)     NOT NULL,
    fecha_nacimiento      DATE            NOT NULL,
    curp                  VARCHAR(18)        NOT NULL,
    rfc                   VARCHAR(13)     NOT NULL,
    sexo                  VARCHAR(10)     NOT NULL,
    nacionalidad          VARCHAR(60)     NOT NULL,
    estado_civil          VARCHAR(20)     NOT NULL,
    correo                VARCHAR(100)    NOT NULL,
    telefono_movil        VARCHAR(10)        NOT NULL,
    telefono_alterno      VARCHAR(10),
    ocupacion             VARCHAR(80)     NOT NULL,
    empresa               VARCHAR(100)    NOT NULL,
    ingreso_mensual       NUMERIC(12,2)   NOT NULL,
    activo                BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    fecha_actualizacion   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_clientes_curp    UNIQUE (curp),
    CONSTRAINT uk_clientes_rfc     UNIQUE (rfc),
    CONSTRAINT uk_clientes_correo  UNIQUE (correo),
    CONSTRAINT ck_clientes_fecha_nac_pasada CHECK (fecha_nacimiento < CURRENT_DATE),
    CONSTRAINT ck_clientes_sexo    CHECK (sexo IN ('MASCULINO','FEMENINO','OTRO')),
    CONSTRAINT ck_clientes_estado_civil CHECK (estado_civil IN
        ('SOLTERO','CASADO','DIVORCIADO','VIUDO','UNION_LIBRE')),
    CONSTRAINT ck_clientes_ingreso_positivo CHECK (ingreso_mensual > 0),
    CONSTRAINT ck_clientes_telefono_movil   CHECK (telefono_movil ~ '^[0-9]{10}$'),
    CONSTRAINT ck_clientes_telefono_alterno CHECK (telefono_alterno IS NULL OR telefono_alterno ~ '^[0-9]{10}$'),
    CONSTRAINT ck_clientes_curp_formato     CHECK (curp ~ '^[A-Z][AEIOUX][A-Z]{2}[0-9]{6}[HMX][A-Z]{5}[A-Z0-9][0-9]$'),
    CONSTRAINT ck_clientes_rfc_formato      CHECK (rfc ~ '^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$')
);

CREATE INDEX idx_clientes_apellido_paterno ON clientes (LOWER(apellido_paterno));
CREATE INDEX idx_clientes_apellido_materno ON clientes (LOWER(apellido_materno));
CREATE INDEX idx_clientes_nombre           ON clientes (LOWER(nombre));
CREATE INDEX idx_clientes_fecha_creacion   ON clientes (fecha_creacion);
CREATE INDEX idx_clientes_activo           ON clientes (activo) WHERE activo = TRUE;

-- Tabla: domicilios -----------------------------------------------------------
CREATE TABLE domicilios (
    id                BIGSERIAL     PRIMARY KEY,
    cliente_id        BIGINT        NOT NULL,
    calle             VARCHAR(120)  NOT NULL,
    numero_exterior   VARCHAR(10)   NOT NULL,
    numero_interior   VARCHAR(10),
    colonia           VARCHAR(80)   NOT NULL,
    municipio         VARCHAR(80)   NOT NULL,
    estado            VARCHAR(60)   NOT NULL,
    codigo_postal     VARCHAR(5)       NOT NULL,
    pais              VARCHAR(60)   NOT NULL DEFAULT 'México',

    CONSTRAINT fk_domicilios_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT uk_domicilios_cliente UNIQUE (cliente_id),
    CONSTRAINT ck_domicilios_cp CHECK (codigo_postal ~ '^[0-9]{5}$')
);

-- Tabla: cuentas --------------------------------------------------------------
CREATE TABLE cuentas (
    id                    BIGSERIAL       PRIMARY KEY,
    numero_cuenta         VARCHAR(18)        NOT NULL,
    cliente_id            BIGINT          NOT NULL,
    saldo                 NUMERIC(15,2)   NOT NULL DEFAULT 0.00,
    estatus               VARCHAR(15)     NOT NULL DEFAULT 'ACTIVA',
    fecha_apertura        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    fecha_actualizacion   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cuentas_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT uk_cuentas_numero  UNIQUE (numero_cuenta),
    CONSTRAINT ck_cuentas_saldo_no_negativo CHECK (saldo >= 0),
    CONSTRAINT ck_cuentas_estatus CHECK (estatus IN
        ('ACTIVA','INACTIVA','BLOQUEADA','CANCELADA')),
    CONSTRAINT ck_cuentas_numero_formato CHECK (numero_cuenta ~ '^[0-9]{18}$')
);

CREATE INDEX idx_cuentas_cliente_id ON cuentas (cliente_id);
CREATE INDEX idx_cuentas_estatus    ON cuentas (estatus);

-- Tabla: usuarios -------------------------------------------------------------
CREATE TABLE usuarios (
    id                    BIGSERIAL      PRIMARY KEY,
    cliente_id            BIGINT         NOT NULL,
    correo                VARCHAR(100)   NOT NULL,
    password              VARCHAR(72)    NOT NULL,
    activo                BOOLEAN        NOT NULL DEFAULT TRUE,
    fecha_creacion        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    fecha_actualizacion   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_usuarios_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT uk_usuarios_cliente UNIQUE (cliente_id),
    CONSTRAINT uk_usuarios_correo  UNIQUE (correo)
);

CREATE INDEX idx_usuarios_correo ON usuarios (correo);

-- Trigger: mantener fecha_actualizacion actualizada ---------------------------
CREATE OR REPLACE FUNCTION set_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_clientes_fecha_actualizacion
    BEFORE UPDATE ON clientes
    FOR EACH ROW EXECUTE FUNCTION set_fecha_actualizacion();

CREATE TRIGGER trg_cuentas_fecha_actualizacion
    BEFORE UPDATE ON cuentas
    FOR EACH ROW EXECUTE FUNCTION set_fecha_actualizacion();

CREATE TRIGGER trg_usuarios_fecha_actualizacion
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION set_fecha_actualizacion();

-- Comentarios (documentacion en la BD) ---------------------------------------
COMMENT ON TABLE clientes  IS 'Personas fisicas onboardeadas. Baja logica via columna activo.';
COMMENT ON TABLE domicilios IS 'Un domicilio por cliente (relacion 1:1).';
COMMENT ON TABLE cuentas   IS 'Cuentas bancarias asociadas a clientes (1:N).';
COMMENT ON TABLE usuarios  IS 'Credenciales de acceso, una por cliente. Password BCrypt.';
