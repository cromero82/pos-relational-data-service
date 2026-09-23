-- Establecimiento (persona natural NO responsable de IVA)
-- Ejecutar en controlneg_rmx_db

CREATE TABLE IF NOT EXISTS establecimiento (
    id                              BIGSERIAL PRIMARY KEY,
    razon_social                    VARCHAR(200) NOT NULL,
    nombre_comercial                VARCHAR(200),
    nit                             VARCHAR(20),
    digito_verificacion             VARCHAR(2),
    regimen_tributario              VARCHAR(40) NOT NULL DEFAULT 'NO_RESPONSABLE_IVA',
    regimen_leyenda_impresion       VARCHAR(300) NOT NULL DEFAULT 'Establecimiento NO RESPONSABLE DE IVA',
    direccion                       VARCHAR(300),
    telefono                        VARCHAR(30),
    email                           VARCHAR(120),
    activo                          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion                  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO establecimiento (
    id, razon_social, nombre_comercial, nit, regimen_tributario, regimen_leyenda_impresion, direccion
)
VALUES (
    1,
    'MI ESTABLECIMIENTO',
    'MI TIENDA POS',
    NULL,
    'NO_RESPONSABLE_IVA',
    'Establecimiento NO RESPONSABLE DE IVA',
    NULL
)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('establecimiento', 'id'), GREATEST((SELECT MAX(id) FROM establecimiento), 1));
