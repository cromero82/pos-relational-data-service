-- 1. Crear el usuario con su contraseña
CREATE USER "romax-admin" WITH PASSWORD 'f4ast3rv3rs10n*';

-- 2. Crear la nueva base de datos
CREATE DATABASE controlneg_rmx_db
    WITH OWNER = "romax-admin"
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Spain.1252'
    LC_CTYPE = 'Spanish_Spain.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Conéctate primero a la base postgres (en DBeaver selecciona esa conexión)
\c postgres;

-- Luego ejecuta:
DROP DATABASE controlneg_rmx_db;



-- 3. Conceder privilegios al usuario sobre la base de datos
GRANT ALL PRIVILEGES ON DATABASE controlneg_rmx_db TO "romax-admin";

-- 4. Conectarse a la nueva base de datos
\c controlneg_rmx_db;

-- 5. Crear el esquema (si quieres uno distinto a public)
CREATE SCHEMA IF NOT EXISTS controlneg_rmx_schema AUTHORIZATION "romax-admin";

-- 6. Dar permisos sobre el esquema
GRANT ALL PRIVILEGES ON SCHEMA controlneg_rmx_schema TO "romax-admin";

-- 7. Opcional: establecer el search_path para que use ese esquema por defecto
ALTER ROLE "romax-admin" SET search_path TO controlneg_rmx_schema, public;


-------------------------

-- 1. Crear el esquema "security" con el mismo usuario como propietario
CREATE SCHEMA IF NOT EXISTS security AUTHORIZATION "romax-admin";

-- 2. Conceder permisos al usuario sobre el esquema
GRANT ALL PRIVILEGES ON SCHEMA security TO "romax-admin";

-- 3. Configurar que el esquema "security" sea el primero en el search_path
ALTER ROLE "romax-admin" SET search_path TO security, public;

----- CORRECCION DE ROL LIMITADO EN BD
-- Esto soluciona el error "denegado el permiso para crear la base de datos"
ALTER ROLE "romax-admin" WITH CREATEDB SUPERUSER;

