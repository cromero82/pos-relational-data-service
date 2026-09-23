-- ============================================================
--  SCRIPT DE CREACIÓN COMPLETA DE BASE DE DATOS
--  Base de datos : controlneg_rmx_db
--  Schemas       : public  (POS / Gestión de Mercado)
--                  security (Autenticación / JWT)
--  Motor         : PostgreSQL 13+
--  Generado      : 2026-04-16
-- ============================================================
-- INSTRUCCIONES DE USO:
--   1. Conéctate a PostgreSQL con un usuario con permisos de superusuario.
--   2. Crea la base de datos si no existe:
--        CREATE DATABASE controlneg_rmx_db;
--   3. Conéctate a la base de datos:
--        \c controlneg_rmx_db
--   4. Ejecuta este script completo.
-- ============================================================


-- ============================================================
-- EXTENSIONES
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";   -- uuid_generate_v4()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";     -- gen_random_uuid()


-- ============================================================
-- SCHEMAS
-- ============================================================

CREATE SCHEMA IF NOT EXISTS security;


-- ============================================================
-- SCHEMA: security
-- Proyecto: infinito-security  (puerto 8081)
-- Tablas  : roles, usuario, usuario_rol, usuario_perfil
-- ============================================================

-- ----------------------------------------------------------
--  security.roles
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS security.roles (
    id     SERIAL       NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    sigla  VARCHAR(50)  NOT NULL,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uq_roles_sigla UNIQUE (sigla)
);

-- ----------------------------------------------------------
--  security.usuario
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS security.usuario (
    id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
    nombre              VARCHAR(255),
    correo_electronico  VARCHAR(255) NOT NULL,
    contrasena          VARCHAR(255) NOT NULL,
    telefono            VARCHAR(255),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_correo UNIQUE (correo_electronico)
);

-- ----------------------------------------------------------
--  security.usuario_rol  (tabla pivote ManyToMany)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS security.usuario_rol (
    usuario_id  UUID    NOT NULL,
    rol_id      INTEGER NOT NULL,

    CONSTRAINT pk_usuario_rol PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_ur_usuario FOREIGN KEY (usuario_id)
        REFERENCES security.usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_rol FOREIGN KEY (rol_id)
        REFERENCES security.roles (id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
--  security.usuario_perfil
--  Nota: referenciado desde pos-relational-data-service
--        con @Table(name = "usuario_perfil", schema = "security")
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS security.usuario_perfil (
    id              BIGSERIAL NOT NULL,
    personalizacion JSONB,
    usuario_id      UUID,

    CONSTRAINT pk_usuario_perfil PRIMARY KEY (id),
    CONSTRAINT fk_up_usuario FOREIGN KEY (usuario_id)
        REFERENCES security.usuario (id) ON DELETE SET NULL
);


-- ============================================================
-- SCHEMA: public
-- Proyecto: pos-relational-data-service  (puerto configurable)
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- BLOQUE 1 – Tablas sin dependencias (catálogos base)
-- ─────────────────────────────────────────────────────────────

-- ----------------------------------------------------------
--  public.tipo_egreso
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_egreso (
    id          BIGSERIAL    NOT NULL,
    nombre      VARCHAR(150) NOT NULL,
    descripcion TEXT         NOT NULL,

    CONSTRAINT pk_tipo_egreso PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.tipo_resultado_fin
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_resultado_fin (
    id          SERIAL       NOT NULL,
    sigla       VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    color       VARCHAR(7),

    CONSTRAINT pk_tipo_resultado_fin PRIMARY KEY (id),
    CONSTRAINT uq_tipo_resultado_fin_sigla UNIQUE (sigla)
);

-- ----------------------------------------------------------
--  public.app_log
--  Almacena logs de nivel WARN y ERROR generados por Log4j2
--  via DbAppender
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_log (
    id        BIGSERIAL    PRIMARY KEY,
    fecha     TIMESTAMP    NOT NULL DEFAULT NOW(),
    nivel     VARCHAR(10)  NOT NULL,
    logger    VARCHAR(255) NOT NULL,
    mensaje   TEXT         NOT NULL,
    excepcion TEXT,
    thread    VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_app_log_fecha  ON app_log (fecha DESC);
CREATE INDEX IF NOT EXISTS idx_app_log_nivel  ON app_log (nivel);

-- ----------------------------------------------------------
--  public.tipo_conflicto
--  Nota: en Java se mapea como enum TipoConflicto,
--        pero la tabla existe en BD para datos de referencia.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS tipo_conflicto (
    id     SERIAL       NOT NULL,
    nombre VARCHAR(100) NOT NULL,

    CONSTRAINT pk_tipo_conflicto PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.configuracion_app
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS configuracion_app (
    id    SERIAL        NOT NULL,
    key   VARCHAR(50)   NOT NULL,
    value VARCHAR(1000) NOT NULL,

    CONSTRAINT pk_configuracion_app PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.evento
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS evento (
    id     SERIAL      NOT NULL,
    nombre VARCHAR(100),
    sigla  VARCHAR(25),

    CONSTRAINT pk_evento PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.estado_recibos
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS estado_recibos (
    id          BIGSERIAL    NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    sigla       VARCHAR(10),

    CONSTRAINT pk_estado_recibos PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.metodo_pago
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS metodo_pago (
    id          BIGSERIAL    NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    estado      VARCHAR(50)  NOT NULL,
    file        VARCHAR(300),
    sigla       VARCHAR(10)  NOT NULL,
    color       VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_metodo_pago PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.client
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS client (
    id        BIGSERIAL    NOT NULL,
    nombre    VARCHAR(255),
    telefono  VARCHAR(50),
    documento VARCHAR(100),

    CONSTRAINT pk_client PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.company
--  Nota: campo description mapeado como TEXT en BD;
--        la entidad JPA usa String sin @Column(columnDefinition="TEXT")
--        lo que puede generar DataException para textos > 255 chars.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS company (
    id           BIGSERIAL    NOT NULL,
    name         VARCHAR(255),
    description  TEXT,
    email        VARCHAR(255),
    telefono     VARCHAR(50),
    contact_name VARCHAR(255),

    CONSTRAINT pk_company PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.producto
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    id                         BIGSERIAL              NOT NULL,
    codigo_barras              VARCHAR(100),
    nombre                     VARCHAR(255)           NOT NULL,
    precio                     NUMERIC(10, 2),
    precio_compra              NUMERIC(10, 2)         NOT NULL DEFAULT 0,
    activate                   INTEGER                NOT NULL DEFAULT 1,
    fecha_actualizacion_precio TIMESTAMP WITHOUT TIME ZONE,
    -- Nota: campo mapeado con timezone en BD pero LocalDateTime en JPA.
    --       Considerar migrar a TIMESTAMP WITH TIME ZONE para evitar
    --       desfases horarios en entornos multi-zona.
    fecha_creacion             TIMESTAMP WITH TIME ZONE,
    total_ventas               INTEGER                         DEFAULT 0,
    fecha_ultima_venta         DATE,
    porcentaje_ganancia        SMALLINT,
    precio_unidad              NUMERIC(10, 2),

    CONSTRAINT pk_producto PRIMARY KEY (id),
    CONSTRAINT uq_producto_codigo_barras UNIQUE (codigo_barras)
);

-- ----------------------------------------------------------
--  public.cargue_productos
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cargue_productos (
    id                       SERIAL       NOT NULL,
    nombre                   VARCHAR(255),
    fecha_creacion           TIMESTAMP WITHOUT TIME ZONE,
    total_migrados           INTEGER,
    total_conflictos         INTEGER,
    total_conflictos_resultos INTEGER,
    mensajes_error           TEXT,

    CONSTRAINT pk_cargue_productos PRIMARY KEY (id)
);


-- ─────────────────────────────────────────────────────────────
-- BLOQUE 2 – Tablas con dependencia de Bloque 1
-- ─────────────────────────────────────────────────────────────

-- ----------------------------------------------------------
--  public.proveedor
--  Nota: proveedor.tipo_egreso_id se crea como BIGINT para
--        coincidir con tipo_egreso.id (BIGSERIAL → BIGINT).
--        El export original mostraba INTEGER, pero PostgreSQL
--        no permite FK entre tipos distintos.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS proveedor (
    id             SERIAL       NOT NULL,
    documento      VARCHAR(50)  NOT NULL,
    nombre         VARCHAR(150) NOT NULL,
    telefono       VARCHAR(30),
    correo         VARCHAR(100),
    tipo_egreso_id BIGINT,

    CONSTRAINT pk_proveedor PRIMARY KEY (id),
    CONSTRAINT uq_proveedor_documento UNIQUE (documento),
    CONSTRAINT fk_proveedor_tipo_egreso FOREIGN KEY (tipo_egreso_id)
        REFERENCES tipo_egreso (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.sesion
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sesion (
    id               BIGSERIAL              NOT NULL,
    cookie           VARCHAR(255),
    ultimo_ticket_id BIGINT,
    -- user_id referencia a security.usuario; sin FK entre schemas
    -- para mantener independencia de los servicios.
    user_id          UUID                   NOT NULL,
    fecha_inicio     TIMESTAMP WITHOUT TIME ZONE,
    fecha_fin        TIMESTAMP WITHOUT TIME ZONE,
    es_activo        BOOLEAN,

    CONSTRAINT pk_sesion PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.bitacora_usuario
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS bitacora_usuario (
    id             SERIAL NOT NULL,
    -- user_id referencia a security.usuario; sin FK entre schemas.
    user_id        UUID,
    evento_id      INTEGER,
    valor_antes    JSONB,
    valor_despues  JSONB,
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE,
    referencia_id  INTEGER,

    CONSTRAINT pk_bitacora_usuario PRIMARY KEY (id),
    CONSTRAINT fk_bitacora_evento FOREIGN KEY (evento_id)
        REFERENCES evento (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.historial_recibo
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_recibo (
    id              BIGSERIAL              NOT NULL,
    cliente_id      BIGINT                 NOT NULL,
    fecha_creacion  TIMESTAMP WITHOUT TIME ZONE,
    estado_id       BIGINT                 NOT NULL,
    metodo_pago_id  BIGINT,
    sesion_id       BIGINT,
    total           NUMERIC(10, 2)         NOT NULL,
    monto_recibido  NUMERIC(10, 2)         NOT NULL,

    CONSTRAINT pk_historial_recibo PRIMARY KEY (id),
    CONSTRAINT fk_hr_cliente FOREIGN KEY (cliente_id)
        REFERENCES client (id),
    CONSTRAINT fk_hr_estado FOREIGN KEY (estado_id)
        REFERENCES estado_recibos (id),
    CONSTRAINT fk_hr_metodo_pago FOREIGN KEY (metodo_pago_id)
        REFERENCES metodo_pago (id) ON DELETE SET NULL,
    CONSTRAINT fk_hr_sesion FOREIGN KEY (sesion_id)
        REFERENCES sesion (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.recibo
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS recibo (
    id              BIGSERIAL              NOT NULL,
    cliente_id      BIGINT                 NOT NULL,
    fecha_creacion  TIMESTAMP WITHOUT TIME ZONE,
    estado_id       BIGINT                 NOT NULL,
    metodo_pago_id  BIGINT,
    sesion_id       BIGINT,
    total           NUMERIC(10, 2)         NOT NULL,
    monto_recibido  NUMERIC(10, 2)         NOT NULL,
    recibo_padre_id BIGINT,

    CONSTRAINT pk_recibo PRIMARY KEY (id),
    CONSTRAINT fk_recibo_cliente FOREIGN KEY (cliente_id)
        REFERENCES client (id),
    CONSTRAINT fk_recibo_estado FOREIGN KEY (estado_id)
        REFERENCES estado_recibos (id),
    CONSTRAINT fk_recibo_metodo_pago FOREIGN KEY (metodo_pago_id)
        REFERENCES metodo_pago (id) ON DELETE SET NULL,
    CONSTRAINT fk_recibo_sesion FOREIGN KEY (sesion_id)
        REFERENCES sesion (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.estadistica_fin
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS estadistica_fin (
    id                   SERIAL                     NOT NULL,
    fecha_creacion       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    total_egresos        NUMERIC(15, 2),
    total_ventas         NUMERIC(15, 2),
    utilidad             NUMERIC(15, 2),
    porcentaje_utilidad  NUMERIC(6, 2),
    formato_tiempo       VARCHAR(10)                NOT NULL,
    valor_tiempo         VARCHAR(20)                NOT NULL,
    tipo_resultado_fin_id INTEGER,

    CONSTRAINT pk_estadistica_fin PRIMARY KEY (id),
    CONSTRAINT fk_ef_tipo_resultado_fin FOREIGN KEY (tipo_resultado_fin_id)
        REFERENCES tipo_resultado_fin (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.corte_venta
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS corte_venta (
    id                        SERIAL                     NOT NULL,
    usuario_id                VARCHAR(36)                NOT NULL,
    fecha_creacion            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    fecha_ini                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    fecha_fin                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    total                     NUMERIC(12, 2),
    total_sistema             NUMERIC(12, 2),
    ultimo_historial_recibo_id BIGINT,

    CONSTRAINT pk_corte_venta PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.cargue_producto_conflictos
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cargue_producto_conflictos (
    id               SERIAL       NOT NULL,
    cargue_prod_id   INTEGER,
    tipo_conflicto_id INTEGER,
    nombre_producto  VARCHAR(255),
    datos_conflicto  JSONB,
    resuelto         BOOLEAN      DEFAULT FALSE,

    CONSTRAINT pk_cargue_producto_conflictos PRIMARY KEY (id),
    CONSTRAINT fk_cpc_cargue_prod FOREIGN KEY (cargue_prod_id)
        REFERENCES cargue_productos (id) ON DELETE CASCADE
);


-- ─────────────────────────────────────────────────────────────
-- BLOQUE 3 – Tablas con dependencia de Bloque 2
-- ─────────────────────────────────────────────────────────────

-- ----------------------------------------------------------
--  public.egreso
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS egreso (
    id             SERIAL                     NOT NULL,
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha          DATE                       NOT NULL,
    valor          NUMERIC(15, 2)             NOT NULL,
    descripcion    TEXT,
    proveedor_id   INTEGER,

    CONSTRAINT pk_egreso PRIMARY KEY (id),
    CONSTRAINT fk_egreso_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedor (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.historial_producto
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_producto (
    id             BIGSERIAL              NOT NULL,
    producto_id    BIGINT,
    evento         VARCHAR(100),
    precio         NUMERIC(10, 2),
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE,
    activo         BOOLEAN,

    CONSTRAINT pk_historial_producto PRIMARY KEY (id),
    CONSTRAINT fk_hp_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.historial_recibo_detalle
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_recibo_detalle (
    id               BIGSERIAL              NOT NULL,
    recibo_id        BIGINT                 NOT NULL,
    producto_id      BIGINT                 NOT NULL,
    cantidad         INTEGER                NOT NULL,
    subtotal         NUMERIC(10, 2)         NOT NULL,
    usuario_creacion UUID,
    fecha_creacion   TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT pk_historial_recibo_detalle PRIMARY KEY (id),
    CONSTRAINT fk_hrd_recibo FOREIGN KEY (recibo_id)
        REFERENCES historial_recibo (id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
--  public.recibo_detalle
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS recibo_detalle (
    id               BIGSERIAL              NOT NULL,
    recibo_id        BIGINT                 NOT NULL,
    producto_id      BIGINT                 NOT NULL,
    cantidad         INTEGER                NOT NULL,
    subtotal         NUMERIC(10, 2)         NOT NULL,
    fecha_creacion   TIMESTAMP WITHOUT TIME ZONE,
    usuario_creacion UUID,

    CONSTRAINT pk_recibo_detalle PRIMARY KEY (id),
    CONSTRAINT fk_rd_recibo FOREIGN KEY (recibo_id)
        REFERENCES recibo (id) ON DELETE CASCADE,
    CONSTRAINT fk_rd_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id)
);

-- ----------------------------------------------------------
--  public.ticket
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ticket (
    id             BIGSERIAL              NOT NULL,
    sesion_id      BIGINT,
    nombre         VARCHAR(255),
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE,
    orden          BIGINT                 NOT NULL DEFAULT 1,

    CONSTRAINT pk_ticket PRIMARY KEY (id),
    CONSTRAINT fk_ticket_sesion FOREIGN KEY (sesion_id)
        REFERENCES sesion (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.ventas_tipo
--  Nota: ventas_tipo.corte_venta_id es BIGINT en el export
--        pero corte_venta.id es INTEGER; no se define FK formal
--        para evitar el mismatch de tipos. La relación es
--        gestionada por JPA (@OneToMany en CorteVenta).
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ventas_tipo (
    id             BIGSERIAL      NOT NULL,
    metodo_pago_id BIGINT,
    total          NUMERIC(10, 2) NOT NULL,
    total_sistema  NUMERIC(10, 2),
    corte_venta_id BIGINT,

    CONSTRAINT pk_ventas_tipo PRIMARY KEY (id),
    CONSTRAINT fk_vt_metodo_pago FOREIGN KEY (metodo_pago_id)
        REFERENCES metodo_pago (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.edicion_recibo
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS edicion_recibo (
    id                  BIGSERIAL              NOT NULL,
    recibo_id           BIGINT,
    historial_recibo_id BIGINT,
    cliente_id          BIGINT                 NOT NULL,
    fecha_creacion      TIMESTAMP WITHOUT TIME ZONE,
    estado_id           BIGINT                 NOT NULL,
    metodo_pago_id      BIGINT,
    sesion_id           BIGINT,
    total               NUMERIC(10, 2)         NOT NULL,
    monto_recibido      NUMERIC(10, 2)         NOT NULL,

    CONSTRAINT pk_edicion_recibo PRIMARY KEY (id),
    CONSTRAINT fk_er_cliente FOREIGN KEY (cliente_id)
        REFERENCES client (id),
    CONSTRAINT fk_er_estado FOREIGN KEY (estado_id)
        REFERENCES estado_recibos (id),
    CONSTRAINT fk_er_metodo_pago FOREIGN KEY (metodo_pago_id)
        REFERENCES metodo_pago (id) ON DELETE SET NULL,
    CONSTRAINT fk_er_sesion FOREIGN KEY (sesion_id)
        REFERENCES sesion (id) ON DELETE SET NULL,
    CONSTRAINT fk_er_recibo FOREIGN KEY (recibo_id)
        REFERENCES recibo (id) ON DELETE SET NULL,
    CONSTRAINT fk_er_historial_recibo FOREIGN KEY (historial_recibo_id)
        REFERENCES historial_recibo (id) ON DELETE SET NULL
);

-- ----------------------------------------------------------
--  public.flujo_dinero
--  Nota: user_id referencia a security.usuario; sin FK
--        entre schemas para independencia de servicios.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS flujo_dinero (
    id       BIGSERIAL      NOT NULL,
    fecha    DATE           NOT NULL,
    tipo_id  SMALLINT       NOT NULL,
    total    NUMERIC(10, 2) NOT NULL,
    user_id  BIGINT,

    CONSTRAINT pk_flujo_dinero PRIMARY KEY (id)
);


-- ─────────────────────────────────────────────────────────────
-- BLOQUE 4 – Tablas con dependencia de Bloque 3
-- ─────────────────────────────────────────────────────────────

-- ----------------------------------------------------------
--  public.recibo_detalle_historico
--  Nota: recibo_detalle_historico.recibo_detalle_id aparece
--        como INTEGER en el export, pero recibo_detalle.id
--        es BIGINT; no se define FK para evitar type mismatch.
--        La entidad JPA usa Long para este campo.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS recibo_detalle_historico (
    id                BIGSERIAL              NOT NULL,
    recibo_detalle_id INTEGER                NOT NULL,
    fecha_hora        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    usuario_id        VARCHAR(36)            NOT NULL,
    accion            VARCHAR(100)           NOT NULL,

    CONSTRAINT pk_recibo_detalle_historico PRIMARY KEY (id)
);

-- ----------------------------------------------------------
--  public.edicion_recibo_detalle
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS edicion_recibo_detalle (
    id          BIGSERIAL      NOT NULL,
    edicion_id  BIGINT         NOT NULL,
    producto_id BIGINT         NOT NULL,
    cantidad    INTEGER        NOT NULL,
    subtotal    NUMERIC(10, 2) NOT NULL,

    CONSTRAINT pk_edicion_recibo_detalle PRIMARY KEY (id),
    CONSTRAINT fk_erd_edicion FOREIGN KEY (edicion_id)
        REFERENCES edicion_recibo (id) ON DELETE CASCADE
);

-- ----------------------------------------------------------
--  public.ticket_recibo
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS ticket_recibo (
    id         BIGSERIAL NOT NULL,
    ticket_id  BIGINT    NOT NULL,
    recibo_id  BIGINT    NOT NULL,

    CONSTRAINT pk_ticket_recibo PRIMARY KEY (id),
    CONSTRAINT uq_ticket_recibo UNIQUE (ticket_id, recibo_id),
    CONSTRAINT fk_tr_ticket FOREIGN KEY (ticket_id)
        REFERENCES ticket (id) ON DELETE CASCADE,
    CONSTRAINT fk_tr_recibo FOREIGN KEY (recibo_id)
        REFERENCES recibo (id) ON DELETE CASCADE
);


-- ============================================================
-- ÍNDICES RECOMENDADOS (rendimiento)
-- ============================================================

-- producto
CREATE INDEX IF NOT EXISTS idx_producto_nombre            ON producto (nombre);
CREATE INDEX IF NOT EXISTS idx_producto_activate          ON producto (activate);

-- recibo
CREATE INDEX IF NOT EXISTS idx_recibo_cliente_id          ON recibo (cliente_id);
CREATE INDEX IF NOT EXISTS idx_recibo_estado_id           ON recibo (estado_id);
CREATE INDEX IF NOT EXISTS idx_recibo_sesion_id           ON recibo (sesion_id);
CREATE INDEX IF NOT EXISTS idx_recibo_fecha_creacion      ON recibo (fecha_creacion);

-- recibo_detalle
CREATE INDEX IF NOT EXISTS idx_rd_recibo_id               ON recibo_detalle (recibo_id);
CREATE INDEX IF NOT EXISTS idx_rd_producto_id             ON recibo_detalle (producto_id);

-- historial_recibo
CREATE INDEX IF NOT EXISTS idx_hr_cliente_id              ON historial_recibo (cliente_id);
CREATE INDEX IF NOT EXISTS idx_hr_fecha_creacion          ON historial_recibo (fecha_creacion);

-- historial_recibo_detalle
CREATE INDEX IF NOT EXISTS idx_hrd_recibo_id              ON historial_recibo_detalle (recibo_id);

-- historial_producto
CREATE INDEX IF NOT EXISTS idx_hp_producto_id             ON historial_producto (producto_id);

-- bitacora_usuario
CREATE INDEX IF NOT EXISTS idx_bu_user_id                 ON bitacora_usuario (user_id);
CREATE INDEX IF NOT EXISTS idx_bu_fecha_creacion          ON bitacora_usuario (fecha_creacion);

-- egreso
CREATE INDEX IF NOT EXISTS idx_egreso_fecha               ON egreso (fecha);
CREATE INDEX IF NOT EXISTS idx_egreso_proveedor_id        ON egreso (proveedor_id);

-- corte_venta
CREATE INDEX IF NOT EXISTS idx_cv_usuario_id              ON corte_venta (usuario_id);
CREATE INDEX IF NOT EXISTS idx_cv_fecha_creacion          ON corte_venta (fecha_creacion);

-- estadistica_fin
CREATE INDEX IF NOT EXISTS idx_ef_fecha_creacion          ON estadistica_fin (fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_ef_formato_valor_tiempo    ON estadistica_fin (formato_tiempo, valor_tiempo);

-- sesion
CREATE INDEX IF NOT EXISTS idx_sesion_user_id             ON sesion (user_id);
CREATE INDEX IF NOT EXISTS idx_sesion_es_activo           ON sesion (es_activo);

-- ticket
CREATE INDEX IF NOT EXISTS idx_ticket_sesion_id           ON ticket (sesion_id);

-- flujo_dinero
CREATE INDEX IF NOT EXISTS idx_fd_fecha                   ON flujo_dinero (fecha);

-- security.usuario
CREATE INDEX IF NOT EXISTS idx_su_activo                  ON security.usuario (activo);

-- security.usuario_perfil
CREATE INDEX IF NOT EXISTS idx_sup_usuario_id             ON security.usuario_perfil (usuario_id);


-- ============================================================
-- DATOS INICIALES (data.sql equivalente)
-- ============================================================

-- Roles base del sistema
INSERT INTO security.roles (nombre, sigla) VALUES
    ('Administrador', 'ADMIN'),
    ('Vendedor',      'VENDEDOR'),
    ('Supervisor',    'SUPERVISOR')
ON CONFLICT (sigla) DO NOTHING;

-- Estados de recibo
INSERT INTO estado_recibos (descripcion, sigla) VALUES
    ('Abierto',    'ABT'),
    ('Pagado',     'PAG'),
    ('Anulado',    'ANU'),
    ('Pendiente',  'PEN')
ON CONFLICT DO NOTHING;

-- Tipos de resultado financiero
INSERT INTO tipo_resultado_fin (sigla, descripcion, color) VALUES
    ('UTIL',  'Utilidad positiva',  '#27ae60'),
    ('PERD',  'Pérdida',            '#e74c3c'),
    ('NEUT',  'Neutro',             '#95a5a6')
ON CONFLICT (sigla) DO NOTHING;

-- Tipos de conflicto (refleja el enum TipoConflicto en Java)
INSERT INTO tipo_conflicto (id, nombre) VALUES
    (1, 'DOS_PRODUCTOS_NOMBRES_IGUALES'),
    (2, 'IGUAL_NOMBRE_Y_CODIGO_BARRAS'),
    (3, 'NO_TIENE_PRECIO')
ON CONFLICT DO NOTHING;

-- Cliente anónimo (requerido como default en recibos)
INSERT INTO client (id, nombre) VALUES (1, 'ANONIMO')
ON CONFLICT DO NOTHING;


-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================
