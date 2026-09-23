CREATE TABLE evento (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    sigla VARCHAR(25)
);

CREATE TABLE bitacora_usuario (
    id SERIAL PRIMARY KEY,
    user_id UUID,
    evento_id INTEGER REFERENCES evento(id),
    valor_antes JSON,
    valor_despues JSON,
    fecha_creacion TIMESTAMP
);

INSERT INTO evento (nombre, sigla) VALUES ('Inicio de sesión', 'INI_SESION');
INSERT INTO evento (nombre, sigla) VALUES ('Finalizar sesión', 'FIN_SESION');
INSERT INTO evento (nombre, sigla) VALUES ('Registrar producto', 'REG_PROD');
INSERT INTO evento (nombre, sigla) VALUES ('Modificar producto', 'MOD_PROD');
INSERT INTO evento (nombre, sigla) VALUES ('Modificar precio producto', 'MOD_PRC_PROD');
INSERT INTO evento (nombre, sigla) VALUES ('Habilitar producto', 'HAB_PROD');
INSERT INTO evento (nombre, sigla) VALUES ('Deshabilitar producto', 'DESHAB_PROD');
INSERT INTO evento (nombre, sigla) VALUES ('Modificar factura', 'MOD_FACT');
INSERT INTO evento (nombre, sigla) VALUES ('Registrar corte de venta', 'REG_CORTE');
INSERT INTO evento (nombre, sigla) VALUES ('Modificar información de usuario', 'MOD_INFO_USR');
INSERT INTO evento (nombre, sigla) VALUES ('Registrar egreso', 'REG_EGRESO');
INSERT INTO evento (nombre, sigla) VALUES ('Modificar egreso', 'MOD_EGRESO');

-- Instrucción ALTER TABLE en caso de ser requerida para modificar la columna existente
ALTER TABLE bitacora_usuario ALTER COLUMN user_id TYPE UUID USING user_id::text::uuid;

-- Agregar campo a la tabla recibo
ALTER TABLE recibo 
ADD COLUMN monto_recibido NUMERIC(10, 2) NOT NULL DEFAULT 0.00;

-- Agregar campo a la tabla historial_recibo
ALTER TABLE historial_recibo 
ADD COLUMN monto_recibido NUMERIC(10, 2) NOT NULL DEFAULT 0.00;

-- Agregar campo a la tabla edicion_recibo
ALTER TABLE edicion_recibo 
ADD COLUMN monto_recibido NUMERIC(10, 2) NOT NULL DEFAULT 0.00;