-- public.app_log definition

-- Drop table

-- DROP TABLE public.app_log;

CREATE TABLE public.app_log (
                                id bigserial NOT NULL,
                                fecha timestamp DEFAULT now() NOT NULL,
                                nivel varchar(10) NOT NULL,
                                logger varchar(255) NOT NULL,
                                mensaje text NOT NULL,
                                excepcion text NULL,
                                thread varchar(100) NULL,
                                CONSTRAINT app_log_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_app_log_fecha ON public.app_log USING btree (fecha DESC);
CREATE INDEX idx_app_log_nivel ON public.app_log USING btree (nivel);

-- Permissions

ALTER TABLE public.app_log OWNER TO "romax-admin";
GRANT ALL ON TABLE public.app_log TO "romax-admin";


-- public.cargue_productos definition

-- Drop table

-- DROP TABLE public.cargue_productos;

CREATE TABLE public.cargue_productos (
                                         id serial4 NOT NULL,
                                         nombre varchar(255) NULL,
                                         fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                         total_migrados int4 DEFAULT 0 NULL,
                                         total_conflictos int4 DEFAULT 0 NULL,
                                         total_conflictos_resultos int4 DEFAULT 0 NULL,
                                         mensajes_error text NULL,
                                         CONSTRAINT cargue_productos_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.cargue_productos OWNER TO "romax-admin";
GRANT ALL ON TABLE public.cargue_productos TO "romax-admin";


-- public.client definition

-- Drop table

-- DROP TABLE public.client;

CREATE TABLE public.client (
                               id bigserial NOT NULL,
                               nombre varchar(255) DEFAULT 'anonimo'::character varying NULL,
                               telefono varchar(50) NULL,
                               documento varchar(100) NULL,
                               CONSTRAINT client_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.client OWNER TO "romax-admin";
GRANT ALL ON TABLE public.client TO "romax-admin";


-- public.company definition

-- Drop table

-- DROP TABLE public.company;

CREATE TABLE public.company (
                                id bigserial NOT NULL,
                                "name" varchar(255) NULL,
                                description text NULL,
                                email varchar(255) NULL,
                                telefono varchar(50) NULL,
                                contact_name varchar(255) NULL,
                                CONSTRAINT company_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.company OWNER TO "romax-admin";
GRANT ALL ON TABLE public.company TO "romax-admin";


-- public.configuracion_app definition

-- Drop table

-- DROP TABLE public.configuracion_app;

CREATE TABLE public.configuracion_app (
                                          id serial4 NOT NULL,
                                          "key" varchar(50) NOT NULL,
                                          value varchar(1000) NOT NULL,
                                          CONSTRAINT configuracion_app_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE public.configuracion_app IS 'Tabla parametrica: preferencias de la aplicacion';

-- Permissions

ALTER TABLE public.configuracion_app OWNER TO "romax-admin";
GRANT ALL ON TABLE public.configuracion_app TO "romax-admin";


-- public.corte_venta definition

-- Drop table

-- DROP TABLE public.corte_venta;

CREATE TABLE public.corte_venta (
                                    id serial4 NOT NULL,
                                    usuario_id varchar(36) NOT NULL,
                                    fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    fecha_ini timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    fecha_fin timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                    total numeric(12, 2) NULL,
                                    total_sistema numeric(12, 2) NULL,
                                    ultimo_historial_recibo_id bigserial NULL,
                                    CONSTRAINT corte_venta_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.corte_venta OWNER TO "romax-admin";
GRANT ALL ON TABLE public.corte_venta TO "romax-admin";


-- public.edicion_recibo definition

-- Drop table

-- DROP TABLE public.edicion_recibo;

CREATE TABLE public.edicion_recibo (
                                       id bigserial NOT NULL,
                                       recibo_id int8 NULL,
                                       historial_recibo_id int8 NULL,
                                       cliente_id int8 NOT NULL,
                                       fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                       estado_id int8 NOT NULL,
                                       metodo_pago_id int8 NULL,
                                       sesion_id int8 NULL,
                                       total numeric(10, 2) NOT NULL,
                                       monto_recibido numeric(10, 2) DEFAULT 0.00 NOT NULL,
                                       CONSTRAINT edicion_recibo_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.edicion_recibo OWNER TO "romax-admin";
GRANT ALL ON TABLE public.edicion_recibo TO "romax-admin";


-- public.estado_recibos definition

-- Drop table

-- DROP TABLE public.estado_recibos;

CREATE TABLE public.estado_recibos (
                                       id bigserial NOT NULL,
                                       descripcion varchar(255) NOT NULL,
                                       sigla varchar(10) NULL,
                                       CONSTRAINT estado_recibos_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE public.estado_recibos IS 'Tabla parametrica: estado de los recibos con respecto a pago';

-- Permissions

ALTER TABLE public.estado_recibos OWNER TO "romax-admin";
GRANT ALL ON TABLE public.estado_recibos TO "romax-admin";


-- public.evento definition

-- Drop table

-- DROP TABLE public.evento;

CREATE TABLE public.evento (
                               id serial4 NOT NULL,
                               nombre varchar(100) NULL,
                               sigla varchar(25) NULL,
                               CONSTRAINT evento_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE public.evento IS 'Tabla parametrica: eventos en la aplicacion con respecto a logica de negocio para bitacora';

-- Permissions

ALTER TABLE public.evento OWNER TO "romax-admin";
GRANT ALL ON TABLE public.evento TO "romax-admin";


-- public.flujo_dinero definition

-- Drop table

-- DROP TABLE public.flujo_dinero;

CREATE TABLE public.flujo_dinero (
                                     id bigserial NOT NULL,
                                     fecha date NOT NULL,
                                     tipo_id int2 NOT NULL,
                                     total numeric(10, 2) NOT NULL,
                                     user_id int8 NULL,
                                     CONSTRAINT flujo_dinero_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.flujo_dinero OWNER TO "romax-admin";
GRANT ALL ON TABLE public.flujo_dinero TO "romax-admin";


-- public.historial_recibo definition

-- Drop table

-- DROP TABLE public.historial_recibo;

CREATE TABLE public.historial_recibo (
                                         id bigserial NOT NULL,
                                         cliente_id int8 NOT NULL,
                                         fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                         estado_id int8 NOT NULL,
                                         metodo_pago_id int8 NULL,
                                         sesion_id int8 NULL,
                                         total numeric(10, 2) NOT NULL,
                                         monto_recibido numeric(10, 2) DEFAULT 0.00 NOT NULL,
                                         CONSTRAINT historial_recibo_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.historial_recibo OWNER TO "romax-admin";
GRANT ALL ON TABLE public.historial_recibo TO "romax-admin";


-- public.metodo_pago definition

-- Drop table

-- DROP TABLE public.metodo_pago;

CREATE TABLE public.metodo_pago (
                                    id bigserial NOT NULL,
                                    descripcion varchar(255) NOT NULL,
                                    estado varchar(50) NOT NULL,
                                    file varchar(300) NULL,
                                    sigla varchar(10) NOT NULL,
                                    color varchar(20) NOT NULL,
                                    CONSTRAINT metodo_pago_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE public.metodo_pago IS 'Tabla parametrica: tipos de metodos de pago en colombia';

-- Permissions

ALTER TABLE public.metodo_pago OWNER TO "romax-admin";
GRANT ALL ON TABLE public.metodo_pago TO "romax-admin";


-- public.producto definition

-- Drop table

-- DROP TABLE public.producto;

CREATE TABLE public.producto (
                                 id bigserial NOT NULL,
                                 codigo_barras varchar(100) NULL,
                                 nombre varchar(255) NOT NULL,
                                 precio numeric(10, 2) NULL,
                                 precio_compra numeric(10, 2) DEFAULT 0 NOT NULL,
                                 activate int4 DEFAULT 1 NOT NULL,
                                 fecha_actualizacion_precio timestamp NULL,
                                 fecha_creacion timestamptz DEFAULT CURRENT_TIMESTAMP NULL,
                                 total_ventas int4 DEFAULT 0 NULL,
                                 fecha_ultima_venta date NULL,
                                 porcentaje_ganancia int2 NULL,
                                 precio_unidad numeric(10, 2) NULL,
                                 CONSTRAINT producto_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.producto OWNER TO "romax-admin";
GRANT ALL ON TABLE public.producto TO "romax-admin";


-- public.recibo definition

-- Drop table

-- DROP TABLE public.recibo;

CREATE TABLE public.recibo (
                               id bigserial NOT NULL,
                               cliente_id int8 NOT NULL,
                               fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                               estado_id int8 NOT NULL,
                               metodo_pago_id int8 NULL,
                               sesion_id int8 NULL,
                               total numeric(10, 2) NOT NULL,
                               monto_recibido numeric(10, 2) DEFAULT 0.00 NOT NULL,
                               recibo_padre_id int8 NULL,
                               CONSTRAINT recibo_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.recibo OWNER TO "romax-admin";
GRANT ALL ON TABLE public.recibo TO "romax-admin";


-- public.recibo_detalle_historico definition

-- Drop table

-- DROP TABLE public.recibo_detalle_historico;

CREATE TABLE public.recibo_detalle_historico (
                                                 id int8 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE) NOT NULL,
                                                 recibo_detalle_id int4 NOT NULL,
                                                 fecha_hora timestamp NOT NULL,
                                                 usuario_id varchar(36) NOT NULL,
                                                 accion varchar(100) NOT NULL,
                                                 CONSTRAINT recibo_detalle_historico_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.recibo_detalle_historico OWNER TO "romax-admin";
GRANT ALL ON TABLE public.recibo_detalle_historico TO "romax-admin";


-- public.reporte_frontend definition

-- Drop table

-- DROP TABLE public.reporte_frontend;

CREATE TABLE public.reporte_frontend (
                                         id uuid NOT NULL,
                                         url text NOT NULL,
                                         actividad_reciente text NOT NULL,
                                         "error" text NOT NULL,
                                         fecha_registro timestamp NOT NULL,
                                         CONSTRAINT reporte_frontend_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.reporte_frontend OWNER TO "romax-admin";
GRANT ALL ON TABLE public.reporte_frontend TO "romax-admin";


-- public.sesion definition

-- Drop table

-- DROP TABLE public.sesion;

CREATE TABLE public.sesion (
                               id bigserial NOT NULL,
                               cookie varchar(255) NULL,
                               ultimo_ticket_id int8 NULL,
                               user_id uuid NULL,
                               fecha_inicio timestamp NULL,
                               fecha_fin timestamp NULL,
                               es_activo bool NULL,
                               CONSTRAINT sesion_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.sesion OWNER TO "romax-admin";
GRANT ALL ON TABLE public.sesion TO "romax-admin";


-- public.ticket_recibo definition

-- Drop table

-- DROP TABLE public.ticket_recibo;

CREATE TABLE public.ticket_recibo (
                                      id bigserial NOT NULL,
                                      ticket_id int8 NOT NULL,
                                      recibo_id int8 NOT NULL,
                                      CONSTRAINT ticket_recibo_pkey PRIMARY KEY (id),
                                      CONSTRAINT uk_ticket_recibo UNIQUE (ticket_id, recibo_id)
);

-- Permissions

ALTER TABLE public.ticket_recibo OWNER TO "romax-admin";
GRANT ALL ON TABLE public.ticket_recibo TO "romax-admin";


-- public.tipo_conflicto definition

-- Drop table

-- DROP TABLE public.tipo_conflicto;

CREATE TABLE public.tipo_conflicto (
                                       id int4 NOT NULL,
                                       nombre varchar(100) NOT NULL,
                                       CONSTRAINT tipo_conflicto_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.tipo_conflicto OWNER TO "romax-admin";
GRANT ALL ON TABLE public.tipo_conflicto TO "romax-admin";


-- public.tipo_egreso definition

-- Drop table

-- DROP TABLE public.tipo_egreso;

CREATE TABLE public.tipo_egreso (
                                    id bigserial NOT NULL,
                                    nombre varchar(150) NOT NULL,
                                    descripcion text DEFAULT ''::text NOT NULL,
                                    CONSTRAINT tipo_egreso_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE public.tipo_egreso IS 'Tabla parametrica: categorias o agrupamiento de un egreso';

-- Permissions

ALTER TABLE public.tipo_egreso OWNER TO "romax-admin";
GRANT ALL ON TABLE public.tipo_egreso TO "romax-admin";


-- public.tipo_resultado_fin definition

-- Drop table

-- DROP TABLE public.tipo_resultado_fin;

CREATE TABLE public.tipo_resultado_fin (
                                           id serial4 NOT NULL,
                                           sigla varchar(50) NOT NULL,
                                           descripcion varchar(255) NOT NULL,
                                           color varchar(7) NULL,
                                           CONSTRAINT tipo_resultado_fin_color_check CHECK (((color)::text ~ '^#[0-9A-Fa-f]{6}$'::text)),
                                           CONSTRAINT tipo_resultado_fin_pkey PRIMARY KEY (id),
                                           CONSTRAINT tipo_resultado_fin_sigla_uk UNIQUE (sigla)
);
COMMENT ON TABLE public.tipo_resultado_fin IS 'Tabla parametrica: tipo de resultado financiero determina un semaforo para visualizar la productividad (basado en %utilidad)';

-- Permissions

ALTER TABLE public.tipo_resultado_fin OWNER TO "romax-admin";
GRANT ALL ON TABLE public.tipo_resultado_fin TO "romax-admin";


-- public.ventas_tipo definition

-- Drop table

-- DROP TABLE public.ventas_tipo;

CREATE TABLE public.ventas_tipo (
                                    id bigserial NOT NULL,
                                    metodo_pago_id int8 NULL,
                                    total numeric(10, 2) NOT NULL,
                                    total_sistema numeric(10, 2) NULL,
                                    corte_venta_id int8 NULL,
                                    CONSTRAINT ventas_tipo_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE public.ventas_tipo OWNER TO "romax-admin";
GRANT ALL ON TABLE public.ventas_tipo TO "romax-admin";


-- public.bitacora_usuario definition

-- Drop table

-- DROP TABLE public.bitacora_usuario;

CREATE TABLE public.bitacora_usuario (
                                         id serial4 NOT NULL,
                                         user_id uuid NULL,
                                         evento_id int4 NULL,
                                         valor_antes json NULL,
                                         valor_despues json NULL,
                                         fecha_creacion timestamp NULL,
                                         referencia_id int4 NULL,
                                         CONSTRAINT bitacora_usuario_pkey PRIMARY KEY (id),
                                         CONSTRAINT bitacora_usuario_evento_id_fkey FOREIGN KEY (evento_id) REFERENCES public.evento(id)
);

-- Permissions

ALTER TABLE public.bitacora_usuario OWNER TO "romax-admin";
GRANT ALL ON TABLE public.bitacora_usuario TO "romax-admin";


-- public.cargue_producto_conflictos definition

-- Drop table

-- DROP TABLE public.cargue_producto_conflictos;

CREATE TABLE public.cargue_producto_conflictos (
                                                   id serial4 NOT NULL,
                                                   cargue_prod_id int4 NULL,
                                                   tipo_conflicto_id int4 NULL,
                                                   nombre_producto varchar(255) NULL,
                                                   datos_conflicto json NULL,
                                                   resuelto bool DEFAULT false NULL,
                                                   CONSTRAINT cargue_producto_conflictos_pkey PRIMARY KEY (id),
                                                   CONSTRAINT fk_cargue FOREIGN KEY (cargue_prod_id) REFERENCES public.cargue_productos(id)
);

-- Permissions

ALTER TABLE public.cargue_producto_conflictos OWNER TO "romax-admin";
GRANT ALL ON TABLE public.cargue_producto_conflictos TO "romax-admin";


-- public.edicion_recibo_detalle definition

-- Drop table

-- DROP TABLE public.edicion_recibo_detalle;

CREATE TABLE public.edicion_recibo_detalle (
                                               id bigserial NOT NULL,
                                               edicion_id int8 NOT NULL,
                                               producto_id int8 NOT NULL,
                                               cantidad int4 NOT NULL,
                                               subtotal numeric(10, 2) NOT NULL,
                                               CONSTRAINT edicion_recibo_detalle_pkey PRIMARY KEY (id),
                                               CONSTRAINT fk_recibo_edic_rec_detalle_recibo FOREIGN KEY (edicion_id) REFERENCES public.edicion_recibo(id)
);

-- Permissions

ALTER TABLE public.edicion_recibo_detalle OWNER TO "romax-admin";
GRANT ALL ON TABLE public.edicion_recibo_detalle TO "romax-admin";


-- public.estadistica_fin definition

-- Drop table

-- DROP TABLE public.estadistica_fin;

CREATE TABLE public.estadistica_fin (
                                        id serial4 NOT NULL,
                                        fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                        total_egresos numeric(15, 2) NULL,
                                        total_ventas numeric(15, 2) NULL,
                                        utilidad numeric(15, 2) NULL,
                                        porcentaje_utilidad numeric(6, 2) NULL,
                                        formato_tiempo varchar(10) NOT NULL,
                                        valor_tiempo varchar(20) NOT NULL,
                                        tipo_resultado_fin_id int4 NULL,
                                        CONSTRAINT estadistica_fin_formato_tiempo_check CHECK (((formato_tiempo)::text = ANY ((ARRAY['DIA'::character varying, 'MES'::character varying, 'ANIO'::character varying])::text[]))),
                                        CONSTRAINT estadistica_fin_pkey PRIMARY KEY (id),
                                        CONSTRAINT fk_tipo_resultado_fin FOREIGN KEY (tipo_resultado_fin_id) REFERENCES public.tipo_resultado_fin(id)
);

-- Permissions

ALTER TABLE public.estadistica_fin OWNER TO "romax-admin";
GRANT ALL ON TABLE public.estadistica_fin TO "romax-admin";


-- public.grupo_espejo definition

-- Drop table

-- DROP TABLE public.grupo_espejo;

CREATE TABLE public.grupo_espejo (
                                     id bigserial NOT NULL,
                                     nombre varchar(100) NOT NULL,
                                     fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                     fecha_actualizacion timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                     producto_referencia_id int8 NULL,
                                     CONSTRAINT grupo_espejo_pkey PRIMARY KEY (id),
                                     CONSTRAINT fk_grupo_espejo_producto FOREIGN KEY (producto_referencia_id) REFERENCES public.producto(id)
);

-- Permissions

ALTER TABLE public.grupo_espejo OWNER TO "romax-admin";
GRANT ALL ON TABLE public.grupo_espejo TO "romax-admin";


-- public.historial_producto definition

-- Drop table

-- DROP TABLE public.historial_producto;

CREATE TABLE public.historial_producto (
                                           id bigserial NOT NULL,
                                           producto_id int8 NULL,
                                           evento varchar(100) NULL,
                                           precio numeric(10, 2) NULL,
                                           fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                           activo bool NULL,
                                           CONSTRAINT historial_producto_pkey PRIMARY KEY (id),
                                           CONSTRAINT fk_historial_producto__producto FOREIGN KEY (producto_id) REFERENCES public.producto(id)
);

-- Permissions

ALTER TABLE public.historial_producto OWNER TO "romax-admin";
GRANT ALL ON TABLE public.historial_producto TO "romax-admin";


-- public.historial_recibo_detalle definition

-- Drop table

-- DROP TABLE public.historial_recibo_detalle;

CREATE TABLE public.historial_recibo_detalle (
                                                 id bigserial NOT NULL,
                                                 recibo_id int8 NOT NULL,
                                                 producto_id int8 NOT NULL,
                                                 cantidad int4 NOT NULL,
                                                 subtotal numeric(10, 2) NOT NULL,
                                                 usuario_creacion uuid NULL,
                                                 fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                                 CONSTRAINT historial_recibo_detalle_pkey PRIMARY KEY (id),
                                                 CONSTRAINT fk_hist_recibo_detalle_recibo FOREIGN KEY (recibo_id) REFERENCES public.historial_recibo(id)
);

-- Permissions

ALTER TABLE public.historial_recibo_detalle OWNER TO "romax-admin";
GRANT ALL ON TABLE public.historial_recibo_detalle TO "romax-admin";


-- public.producto_espejo definition

-- Drop table

-- DROP TABLE public.producto_espejo;

CREATE TABLE public.producto_espejo (
                                        id bigserial NOT NULL,
                                        grupo_espejo_id int8 NOT NULL,
                                        producto_id int8 NOT NULL,
                                        fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                        CONSTRAINT producto_espejo_pkey PRIMARY KEY (id),
                                        CONSTRAINT fk_producto_espejo_grupo FOREIGN KEY (grupo_espejo_id) REFERENCES public.grupo_espejo(id),
                                        CONSTRAINT fk_producto_espejo_producto FOREIGN KEY (producto_id) REFERENCES public.producto(id)
);

-- Permissions

ALTER TABLE public.producto_espejo OWNER TO "romax-admin";
GRANT ALL ON TABLE public.producto_espejo TO "romax-admin";


-- public.proveedor definition

-- Drop table

-- DROP TABLE public.proveedor;

CREATE TABLE public.proveedor (
                                  id serial4 NOT NULL,
                                  documento varchar(50) NOT NULL,
                                  nombre varchar(150) NOT NULL,
                                  telefono varchar(30) NULL,
                                  correo varchar(100) NULL,
                                  tipo_egreso_id int4 NULL,
                                  CONSTRAINT proveedor_documento_key UNIQUE (documento),
                                  CONSTRAINT proveedor_pkey PRIMARY KEY (id),
                                  CONSTRAINT fk_proveedor_tipo_egreso FOREIGN KEY (tipo_egreso_id) REFERENCES public.tipo_egreso(id)
);

-- Permissions

ALTER TABLE public.proveedor OWNER TO "romax-admin";
GRANT ALL ON TABLE public.proveedor TO "romax-admin";


-- public.recibo_detalle definition

-- Drop table

-- DROP TABLE public.recibo_detalle;

CREATE TABLE public.recibo_detalle (
                                       id bigserial NOT NULL,
                                       recibo_id int8 NOT NULL,
                                       producto_id int8 NOT NULL,
                                       cantidad int4 NOT NULL,
                                       subtotal numeric(10, 2) NOT NULL,
                                       fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                                       usuario_creacion uuid NULL,
                                       CONSTRAINT recibo_detalle_pkey PRIMARY KEY (id),
                                       CONSTRAINT fk_recibo_detalle_producto FOREIGN KEY (producto_id) REFERENCES public.producto(id),
                                       CONSTRAINT fk_recibo_detalle_recibo FOREIGN KEY (recibo_id) REFERENCES public.recibo(id)
);

-- Permissions

ALTER TABLE public.recibo_detalle OWNER TO "romax-admin";
GRANT ALL ON TABLE public.recibo_detalle TO "romax-admin";


-- public.ticket definition

-- Drop table

-- DROP TABLE public.ticket;

CREATE TABLE public.ticket (
                               id bigserial NOT NULL,
                               sesion_id int8 NULL,
                               nombre varchar(255) NULL,
                               fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NULL,
                               orden int8 DEFAULT 1 NOT NULL,
                               CONSTRAINT ticket_pkey PRIMARY KEY (id),
                               CONSTRAINT fk_ticket_sesion FOREIGN KEY (sesion_id) REFERENCES public.sesion(id)
);

-- Permissions

ALTER TABLE public.ticket OWNER TO "romax-admin";
GRANT ALL ON TABLE public.ticket TO "romax-admin";


-- public.egreso definition

-- Drop table

-- DROP TABLE public.egreso;

CREATE TABLE public.egreso (
                               id serial4 NOT NULL,
                               fecha_creacion timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
                               fecha date NOT NULL,
                               valor numeric(15, 2) NOT NULL,
                               descripcion text NULL,
                               proveedor_id int4 NULL,
                               CONSTRAINT egreso_pkey PRIMARY KEY (id),
                               CONSTRAINT fk_proveedor FOREIGN KEY (proveedor_id) REFERENCES public.proveedor(id)
);

-- Permissions

ALTER TABLE public.egreso OWNER TO "romax-admin";
GRANT ALL ON TABLE public.egreso TO "romax-admin";

--------------------------------------------
-- Security Schema
--------------------------------------------

-- "security".roles definition

-- Drop table

-- DROP TABLE "security".roles;

CREATE TABLE "security".roles (
                                  id serial4 NOT NULL,
                                  nombre varchar(100) NOT NULL,
                                  sigla varchar(50) NOT NULL,
                                  CONSTRAINT roles_pkey PRIMARY KEY (id),
                                  CONSTRAINT roles_sigla_key UNIQUE (sigla)
);
COMMENT ON TABLE "security".roles IS 'Tabla parametrica: roles del sistema';

-- Permissions

ALTER TABLE "security".roles OWNER TO "romax-admin";
GRANT ALL ON TABLE "security".roles TO "romax-admin";


-- "security".usuario definition

-- Drop table

-- DROP TABLE "security".usuario;

CREATE TABLE "security".usuario (
                                    id uuid DEFAULT gen_random_uuid() NOT NULL,
                                    nombre varchar(255) NULL,
                                    correo_electronico varchar(255) NOT NULL,
                                    contrasena varchar(255) NOT NULL,
                                    telefono varchar(50) NULL,
                                    activo bool DEFAULT true NULL,
                                    CONSTRAINT usuario_correo_electronico_key UNIQUE (correo_electronico),
                                    CONSTRAINT usuario_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE "security".usuario OWNER TO "romax-admin";
GRANT ALL ON TABLE "security".usuario TO "romax-admin";


-- "security".usuario_perfil definition

-- Drop table

-- DROP TABLE "security".usuario_perfil;

CREATE TABLE "security".usuario_perfil (
                                           id serial4 NOT NULL,
                                           usuario_id uuid NULL,
                                           personalizacion jsonb NULL,
                                           CONSTRAINT usuario_perfil_pkey PRIMARY KEY (id)
);

-- Permissions

ALTER TABLE "security".usuario_perfil OWNER TO "romax-admin";
GRANT ALL ON TABLE "security".usuario_perfil TO "romax-admin";


-- "security".usuario_rol definition

-- Drop table

-- DROP TABLE "security".usuario_rol;

CREATE TABLE "security".usuario_rol (
                                        id serial4 NOT NULL,
                                        usuario_id uuid NOT NULL,
                                        rol_id int4 NOT NULL,
                                        CONSTRAINT usuario_rol_pkey PRIMARY KEY (id),
                                        CONSTRAINT fk_rol FOREIGN KEY (rol_id) REFERENCES "security".roles(id) ON DELETE CASCADE,
                                        CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES "security".usuario(id) ON DELETE CASCADE
);

-- Permissions

ALTER TABLE "security".usuario_rol OWNER TO "romax-admin";
GRANT ALL ON TABLE "security".usuario_rol TO "romax-admin";


-- "security".usuario_roles definition

-- Drop table

-- DROP TABLE "security".usuario_roles;

CREATE TABLE "security".usuario_roles (
                                          usuario_id uuid NOT NULL,
                                          rol_id int4 NOT NULL,
                                          CONSTRAINT usuario_roles_pkey PRIMARY KEY (usuario_id, rol_id),
                                          CONSTRAINT usuario_roles_rol_id_fkey FOREIGN KEY (rol_id) REFERENCES "security".roles(id) ON DELETE CASCADE,
                                          CONSTRAINT usuario_roles_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES "security".usuario(id) ON DELETE CASCADE
);

-- Permissions

ALTER TABLE "security".usuario_roles OWNER TO "romax-admin";
GRANT ALL ON TABLE "security".usuario_roles TO "romax-admin";