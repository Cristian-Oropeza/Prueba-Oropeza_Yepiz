-- ============================================================================
-- Modulo de Pagos - Integracion con GestoPago (sendTx / confirmTx)
-- ============================================================================
-- Alineado con el documento de referencia del modulo de Pagos:
--   * Tabla "transacciones" como registro maestro de cada intento de operacion.
--   * Catalogo de productos local con reglas de tipoFront y precio fijo.
--   * ALTER a "cuentas" para agregar columna "version" (bloqueo optimista JPA).
-- ============================================================================

-- Tabla: catalogo_productos ---------------------------------------------------
CREATE TABLE catalogo_productos (
    id                       BIGSERIAL       PRIMARY KEY,
    id_servicio              VARCHAR(50)     NOT NULL,
    id_producto              VARCHAR(50)     NOT NULL,
    descripcion              VARCHAR(200)    NOT NULL,
    tipo_front               SMALLINT        NOT NULL,
    id_cat_tipo_servicio     SMALLINT,
    precio                   NUMERIC(15,2),
    monto_min                NUMERIC(15,2)   NOT NULL,
    monto_max                NUMERIC(15,2)   NOT NULL,
    moneda                   CHAR(3)         NOT NULL DEFAULT 'MXN',
    activo                   BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    fecha_actualizacion      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_catalogo_productos_servicio_producto UNIQUE (id_servicio, id_producto),
    CONSTRAINT ck_catalogo_productos_tipo_front   CHECK (tipo_front IN (1, 2)),
    CONSTRAINT ck_catalogo_productos_monto_min    CHECK (monto_min > 0),
    CONSTRAINT ck_catalogo_productos_monto_max    CHECK (monto_max >= monto_min),
    CONSTRAINT ck_catalogo_productos_moneda       CHECK (moneda ~ '^[A-Z]{3}$')
);
CREATE INDEX idx_catalogo_productos_activo ON catalogo_productos (activo) WHERE activo = TRUE;

-- Tabla: transacciones --------------------------------------------------------
CREATE TABLE transacciones (
    id                        BIGSERIAL       PRIMARY KEY,
    referencia_operacion      VARCHAR(40)     NOT NULL,
    idempotency_key           VARCHAR(80)     NOT NULL,
    cliente_id                BIGINT          NOT NULL,
    cuenta_id                 BIGINT          NOT NULL,
    id_servicio               VARCHAR(50)     NOT NULL,
    id_producto               VARCHAR(50)     NOT NULL,
    telefono                  VARCHAR(20),
    referencia                VARCHAR(80),
    monto                     NUMERIC(15,2)   NOT NULL,
    moneda                    CHAR(3)         NOT NULL DEFAULT 'MXN',
    estado                    VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    upc                       VARCHAR(15)     NOT NULL,
    unidad                    VARCHAR(25)     NOT NULL,
    hora_local                VARCHAR(14),
    payload_enviado           TEXT,
    codigo_proveedor          VARCHAR(10),
    texto_proveedor           VARCHAR(500),
    id_tx_proveedor           VARCHAR(80),
    num_autorizacion          BIGINT,
    intentos_conciliacion     INTEGER         NOT NULL DEFAULT 0,
    fecha_envio_proveedor     TIMESTAMPTZ,
    fecha_confirmacion        TIMESTAMPTZ,
    fecha_ultima_conciliacion TIMESTAMPTZ,
    fecha_creacion            TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    fecha_actualizacion       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_transacciones_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transacciones_cuenta  FOREIGN KEY (cuenta_id)  REFERENCES cuentas(id)  ON DELETE RESTRICT,
    CONSTRAINT uk_transacciones_idempotency UNIQUE (idempotency_key),
    CONSTRAINT uk_transacciones_referencia  UNIQUE (referencia_operacion),
    CONSTRAINT uk_transacciones_upc         UNIQUE (upc),
    CONSTRAINT ck_transacciones_monto_pos   CHECK (monto > 0),
    CONSTRAINT ck_transacciones_moneda      CHECK (moneda ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_transacciones_estado      CHECK (estado IN
        ('PENDIENTE','EN_CONFIRMACION','APLICADA','RECHAZADA','NO_ENVIADA')),
    CONSTRAINT ck_transacciones_intentos    CHECK (intentos_conciliacion >= 0)
);
CREATE INDEX idx_transacciones_cliente_id          ON transacciones (cliente_id);
CREATE INDEX idx_transacciones_cuenta_estado_fecha ON transacciones (cuenta_id, estado, fecha_creacion);
CREATE INDEX idx_transacciones_estado_envio        ON transacciones (estado, fecha_envio_proveedor)
    WHERE estado = 'EN_CONFIRMACION';

-- ALTER cuentas: bloqueo optimista -------------------------------------------
ALTER TABLE cuentas ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Triggers de fecha_actualizacion (reusa la funcion set_fecha_actualizacion de V1)
CREATE TRIGGER trg_catalogo_productos_fecha_actualizacion
    BEFORE UPDATE ON catalogo_productos
    FOR EACH ROW EXECUTE FUNCTION set_fecha_actualizacion();

CREATE TRIGGER trg_transacciones_fecha_actualizacion
    BEFORE UPDATE ON transacciones
    FOR EACH ROW EXECUTE FUNCTION set_fecha_actualizacion();

-- Seed de catalogo: productos basicos del distribuidor 83 de PuntoRed --------
-- tipo_front: 1 = recarga con precio fijo (monto == precio); 2 = pago de servicio con referencia.
-- id_cat_tipo_servicio 10/11 = canjeables (telefono "#1111111111"); otro = telefono normal.
INSERT INTO catalogo_productos (id_servicio, id_producto, descripcion, tipo_front, id_cat_tipo_servicio, precio, monto_min, monto_max) VALUES
    ('TELCEL',   'TELCEL_050',  'Recarga Telcel $50',    1,  1,    50.00,    50.00,    50.00),
    ('TELCEL',   'TELCEL_100',  'Recarga Telcel $100',   1,  1,   100.00,   100.00,   100.00),
    ('TELCEL',   'TELCEL_200',  'Recarga Telcel $200',   1,  1,   200.00,   200.00,   200.00),
    ('TELCEL',   'TELCEL_500',  'Recarga Telcel $500',   1,  1,   500.00,   500.00,   500.00),
    ('MOVISTAR', 'MOV_100',     'Recarga Movistar $100', 1,  1,   100.00,   100.00,   100.00),
    ('ATT',      'ATT_200',     'Recarga AT&T $200',     1,  1,   200.00,   200.00,   200.00),
    ('CFE',      'CFE_PAGO',    'Pago recibo CFE',       2,  2,   NULL,      10.00, 50000.00),
    ('AGUA_CDMX','AGUA_PAGO',   'Pago Agua CDMX',        2,  2,   NULL,      10.00, 20000.00),
    ('IZZI',     'IZZI_PAGO',   'Pago Izzi',             2,  2,   NULL,     100.00, 10000.00),
    ('TELMEX',   'TELMEX_PAGO', 'Pago Telmex',           2,  2,   NULL,     100.00, 10000.00);

COMMENT ON TABLE catalogo_productos IS 'Catalogo local de servicios/productos. Reglas de tipoFront aqui.';
COMMENT ON TABLE transacciones      IS 'Registro maestro de cada transaccion contra GestoPago. idempotency_key UNIQUE previene cargos duplicados.';
COMMENT ON COLUMN cuentas.version    IS 'Columna de bloqueo optimista (@Version JPA) para evitar race conditions en el saldo.';
