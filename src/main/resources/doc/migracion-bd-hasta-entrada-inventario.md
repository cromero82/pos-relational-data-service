# Migración BD — hasta `entrada_inventario`

## Objetivo

Llevar la base de datos `controlneg_rmx_db` desde el estado anterior a la funcionalidad de entradas de inventario, incluyendo:

- Tablas nuevas: `entrada_inventario`, `entrada_inventario_detalle`, `historial_precio_producto`
- Columna `existencia` en `producto`
- Columna `precio_venta_nuevo` en `entrada_inventario_detalle`
- Índices faltantes y constraint `UNIQUE` en `producto.codigo_referencia`
- Evento `ENTRADA_INV_PRECIO`
- Secuencias para las nuevas tablas
- Resincronización de todas las secuencias (`fixbd.sql`)
- Homologación de `NOT NULL` en `sesion.user_id` y `configuracion_app.app_id`

## Estado inicial (pre-migración)

Se tomó como línea base el documento `basedatos-antes-dbflux-y-entradas-inventario.md`.

Tablas existentes (confirmado vía `\dt`):

- `producto`, `proveedor`, `egreso`, `recibo`, `recibo_detalle`, `corte_venta`, `estadistica_fin`, `flujo_dinero`, `sesion`, `ticket`, `bitacora_usuario`, `historial_producto`, `historial_recibo`, `historial_recibo_detalle`, `evento`, `configuracion_app`, `usuario_perfil`, `metodo_pago`, `tipo_egreso`, `tipo_resultado_fin`, `ventas_tipo`, `cargue_productos`, `cargue_producto_conflictos`, `grupo_espejo`, `edicion_recibo`, `edicion_recibo_detalle`, `estado_recibos`, `client`, `app_log`

## Cambios aplicados

### 1. Columna `existencia` en `producto`

```sql
ALTER TABLE producto ADD COLUMN IF NOT EXISTS existencia NUMERIC(12,2) NOT NULL DEFAULT 0;
```

### 2. Constraint `UNIQUE` en `producto.codigo_referencia`

```sql
ALTER TABLE producto ADD UNIQUE (codigo_referencia);
```

### 3. Índices faltantes

```sql
CREATE INDEX IF NOT EXISTS idx_recibo_usuario_cerro_id    ON recibo (usuario_cerro_id);

CREATE INDEX IF NOT EXISTS idx_egreso_proveedor_id        ON egreso (proveedor_id);

CREATE INDEX IF NOT EXISTS idx_corte_venta_usuario_id     ON corte_venta (usuario_id);

CREATE INDEX IF NOT EXISTS idx_estadistica_fin_usuario_id     ON estadistica_fin (usuario_id);
CREATE INDEX IF NOT EXISTS idx_estadistica_fin_corte_caja_id  ON estadistica_fin (corte_caja_id);

CREATE INDEX IF NOT EXISTS idx_flujo_dinero_usuario_id    ON flujo_dinero (usuario_id);
CREATE INDEX IF NOT EXISTS idx_flujo_dinero_egreso_id     ON flujo_dinero (egreso_id);

CREATE INDEX IF NOT EXISTS idx_sesion_user_id             ON sesion (user_id);

CREATE INDEX IF NOT EXISTS idx_ticket_sesion_id           ON ticket (sesion_id);
```

### 4. `NOT NULL` en `sesion.user_id`

```sql
ALTER TABLE sesion ALTER COLUMN user_id SET NOT NULL;
```

> No se encontraron filas con `user_id IS NULL`, por lo que la migración fue segura.

### 5. `NOT NULL` en `configuracion_app.app_id`

```sql
ALTER TABLE configuracion_app ALTER COLUMN app_id SET NOT NULL;
```

> No se encontraron filas con `app_id IS NULL`, por lo que la migración fue segura.

### 6. Evento `ENTRADA_INV_PRECIO`

```sql
INSERT INTO evento (nombre, sigla) VALUES ('Entrada inventario precio', 'ENTRADA_INV_PRECIO') ON CONFLICT DO NOTHING;
```

### 7. Secuencias para nuevas tablas

```sql
CREATE SEQUENCE IF NOT EXISTS entrada_inventario_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS entrada_inventario_detalle_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS historial_precio_producto_id_seq START WITH 1 INCREMENT BY 1;
```

### 8. Tabla `entrada_inventario`

```sql
CREATE TABLE IF NOT EXISTS entrada_inventario (
    id               BIGINT       NOT NULL DEFAULT nextval('entrada_inventario_id_seq'),
    egreso_id        BIGINT       NOT NULL,
    estado           VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT NOW(),
    fecha_confirmacion TIMESTAMP,
    total_items      INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT entrada_inventario_pkey PRIMARY KEY (id),
    CONSTRAINT entrada_inventario_egreso_id_fkey FOREIGN KEY (egreso_id) REFERENCES egreso(id)
);

CREATE INDEX IF NOT EXISTS idx_entrada_inventario_egreso_id ON entrada_inventario (egreso_id);
CREATE INDEX IF NOT EXISTS idx_entrada_inventario_estado    ON entrada_inventario (estado);
```

### 9. Tabla `entrada_inventario_detalle`

```sql
CREATE TABLE IF NOT EXISTS entrada_inventario_detalle (
    id                      BIGINT        NOT NULL DEFAULT nextval('entrada_inventario_detalle_id_seq'),
    entrada_id              BIGINT        NOT NULL,
    producto_id             BIGINT        NOT NULL,
    cantidad                NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_compra_registrado NUMERIC(12,2),
    precio_compra_anterior  NUMERIC(12,2),
    precio_venta_actual     NUMERIC(12,2),
    precio_venta_nuevo      NUMERIC(12,2),
    porcentaje_ganancia_calc NUMERIC(5,2),
    CONSTRAINT entrada_inventario_detalle_pkey PRIMARY KEY (id),
    CONSTRAINT entrada_inventario_detalle_entrada_id_fkey FOREIGN KEY (entrada_id) REFERENCES entrada_inventario(id),
    CONSTRAINT entrada_inventario_detalle_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES producto(id),
    CONSTRAINT entrada_inventario_detalle_cantidad_check CHECK (cantidad >= 0),
    CONSTRAINT entrada_inventario_detalle_precio_compra_check CHECK (precio_compra_registrado IS NULL OR precio_compra_registrado >= 0)
);

CREATE INDEX IF NOT EXISTS idx_entrada_inventario_detalle_entrada_id  ON entrada_inventario_detalle (entrada_id);
CREATE INDEX IF NOT EXISTS idx_entrada_inventario_detalle_producto_id ON entrada_inventario_detalle (producto_id);
```

### 10. Tabla `historial_precio_producto`

```sql
CREATE TABLE IF NOT EXISTS historial_precio_producto (
    id                          BIGINT        NOT NULL DEFAULT nextval('historial_precio_producto_id_seq'),
    entrada_inventario_detalle_id BIGINT      NOT NULL,
    producto_id                 BIGINT        NOT NULL,
    fecha_creacion              TIMESTAMP     NOT NULL DEFAULT NOW(),
    precio_compra               NUMERIC(12,2),
    precio_compra_antes         NUMERIC(12,2),
    precio_venta                NUMERIC(12,2),
    precio_venta_antes          NUMERIC(12,2),
    porcentaje_ganancia         NUMERIC(5,2),
    porcentaje_ganancia_antes   NUMERIC(5,2),
    CONSTRAINT historial_precio_producto_pkey PRIMARY KEY (id),
    CONSTRAINT historial_precio_producto_detalle_id_fkey FOREIGN KEY (entrada_inventario_detalle_id) REFERENCES entrada_inventario_detalle(id),
    CONSTRAINT historial_precio_producto_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES producto(id)
);

CREATE INDEX IF NOT EXISTS idx_historial_precio_producto_detalle_id ON historial_precio_producto (entrada_inventario_detalle_id);
CREATE INDEX IF NOT EXISTS idx_historial_precio_producto_producto_id ON historial_precio_producto (producto_id);
CREATE INDEX IF NOT EXISTS idx_historial_precio_producto_fecha      ON historial_precio_producto (fecha_creacion DESC);
```

### 11. Resincronización de secuencias

Se ejecutó el script `fixbd.sql` completo para ajustar todas las secuencias al valor máximo actual de cada tabla.

## Estado final (post-migración)

```
controlneg_rmx_db
├── producto              (+ existencia, + UNIQUE codigo_referencia)
├── entrada_inventario    (nueva)
├── entrada_inventario_detalle (nueva, + precio_venta_nuevo)
├── historial_precio_producto (nueva)
├── sesion                (user_id NOT NULL)
├── configuracion_app     (app_id NOT NULL)
├── evento                (+ ENTRADA_INV_PRECIO)
└── (demás tablas sin cambios)
```

Todas las secuencias sincronizadas.

## Seed de datos de prueba

El archivo `historial_precio_producto_seed_chococono.sql` contiene datos de ejemplo para el producto CHOCOCONO (id 656). Se puede ejecutar para poblar las tablas con 6 cambios de precio históricos simulados.

```bash
PGPASSWORD='f4ast3rv3rs10n*' psql -h localhost -U romax-admin -d controlneg_rmx_db \
  -f src/main/resources/doc/contextos/database/historial_precio_producto_seed_chococono.sql
```
