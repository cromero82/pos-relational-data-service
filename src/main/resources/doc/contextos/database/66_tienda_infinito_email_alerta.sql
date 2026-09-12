-- Correo de banco de la pila v02 (Cloudflare Email Routing → Worker → puente :8295).
-- 28_confirmacion deja pagos@ (pruebas). En Tienda Infinito MUST ser tienda-infinito@.
-- Solo sobre controlneg_rmx_db_v02.

UPDATE establecimiento
SET email_alerta_pagos = 'tienda-infinito@mayaksoluciones.com'
WHERE id = 1;

COMMENT ON COLUMN establecimiento.email_alerta_pagos IS
    'Correo de alerta bancaria. v02 / Tienda Infinito: tienda-infinito@mayaksoluciones.com';
