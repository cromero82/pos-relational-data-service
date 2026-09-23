-- Sprint 4 — Catálogo permisos pantalla POS (referencia roles JWT por sigla)

CREATE TABLE IF NOT EXISTS funcionalidad_pos (
    id                  SERIAL PRIMARY KEY,
    codigo              VARCHAR(50) NOT NULL UNIQUE,
    nombre              VARCHAR(100) NOT NULL,
    seccion             VARCHAR(50),
    ruta_front          VARCHAR(200),
    etiqueta_menu       VARCHAR(100),
    badge_ui            VARCHAR(40),
    requiere_disclaimer BOOLEAN NOT NULL DEFAULT FALSE,
    activo              BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS funcionalidad_rol (
    funcionalidad_id    INTEGER NOT NULL REFERENCES funcionalidad_pos (id) ON DELETE CASCADE,
    rol_sigla           VARCHAR(50) NOT NULL,
    puede_leer          BOOLEAN NOT NULL DEFAULT TRUE,
    puede_escribir      BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (funcionalidad_id, rol_sigla)
);

INSERT INTO funcionalidad_pos (codigo, nombre, seccion, ruta_front, etiqueta_menu, badge_ui, requiere_disclaimer)
VALUES
    ('TICKETS', 'Tickets / ventas', 'VENTAS', '/apps/tickets', 'Tickets', NULL, FALSE),
    ('HISTORIAL_VENTAS', 'Historial ventas', 'VENTAS', '/apps/tickets/historial', 'Historial Tickets', NULL, FALSE),
    ('EGRESOS', 'Egresos', 'FINANCIERO', '/apps/financiero/egresos', 'Egresos', 'Salidas de caja', FALSE),
    ('RESUMEN_ECONOMICO', 'Resumen económico', 'FINANCIERO', '/apps/financiero/resumen-economico', 'Resumen económico', 'Ayuda gerencial', TRUE),
    ('CIERRE_TURNO', 'Cierre de turno', 'FINANCIERO', '/apps/financiero/ingresos', 'Ingresos', 'Cortes de venta', FALSE),
    ('ENTRADA_ALMACEN', 'Entrada almacén', 'FINANCIERO', NULL, NULL, 'Operativo', FALSE),
    ('BACKUP_BD', 'Backup BD', 'ADMIN', NULL, 'Backup', 'Solo admin', FALSE),
    ('GESTION_USUARIOS', 'Gestión usuarios', 'ADMIN', '/apps/gestion-usuarios', NULL, NULL, FALSE),
    ('ANULAR_VENTA', 'Anular venta', 'VENTAS', NULL, NULL, NULL, FALSE),
    ('RESTAURAR_TICKET', 'Restaurar ticket', 'VENTAS', NULL, NULL, NULL, FALSE)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO funcionalidad_rol (funcionalidad_id, rol_sigla, puede_leer, puede_escribir)
SELECT f.id, r.rol_sigla, r.puede_leer, r.puede_escribir
FROM funcionalidad_pos f
CROSS JOIN (VALUES
    ('TICKETS', 'admin', TRUE, TRUE),
    ('TICKETS', 'cajero', TRUE, TRUE),
    ('TICKETS', 'invitado', TRUE, FALSE),
    ('HISTORIAL_VENTAS', 'admin', TRUE, TRUE),
    ('HISTORIAL_VENTAS', 'cajero', TRUE, TRUE),
    ('HISTORIAL_VENTAS', 'invitado', TRUE, FALSE),
    ('EGRESOS', 'admin', TRUE, TRUE),
    ('EGRESOS', 'cajero', TRUE, TRUE),
    ('RESUMEN_ECONOMICO', 'admin', TRUE, FALSE),
    ('CIERRE_TURNO', 'admin', TRUE, TRUE),
    ('CIERRE_TURNO', 'cajero', TRUE, TRUE),
    ('ENTRADA_ALMACEN', 'admin', TRUE, TRUE),
    ('ENTRADA_ALMACEN', 'cajero', TRUE, TRUE),
    ('BACKUP_BD', 'admin', TRUE, TRUE),
    ('GESTION_USUARIOS', 'admin', TRUE, TRUE),
    ('ANULAR_VENTA', 'admin', TRUE, TRUE),
    ('ANULAR_VENTA', 'cajero', TRUE, TRUE),
    ('RESTAURAR_TICKET', 'admin', TRUE, TRUE),
    ('RESTAURAR_TICKET', 'cajero', TRUE, TRUE)
) AS r(codigo, rol_sigla, puede_leer, puede_escribir)
WHERE f.codigo = r.codigo
ON CONFLICT (funcionalidad_id, rol_sigla) DO NOTHING;
