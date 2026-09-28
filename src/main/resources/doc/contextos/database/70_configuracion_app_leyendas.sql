-- Leyendas de configuracion_app para el modal Ajustes configurables del sistema.
-- Solo actualiza leyenda. NO toca value ni key. Idempotente.
-- Destino: controlneg_rmx_db_v02.

UPDATE configuracion_app c
SET leyenda = s.ley
FROM (VALUES
    ('alerta-precios',
     'En edicion de precios de productos lanza una alerta visual, si al modificar el precio el nuevo'),
    ('notificaciones.activa',
     'Se habilitan o no las notificaciones'),
    ('notificaciones.asociaciones-egresos.obligatorio',
     'Se habilitan que las notificaciones de medios electrónicos se asocien a egresos'),
    ('corte-venta.base-efectivo',
     'Base dinero en efectivo sugerida para las cajas'),
    ('notificaciones.qr.asuntos-permitidos',
     'Notificaciones que seran almacenadas, el valor corresponde al campo [Asunto] dentro del correo electrónico'),
    ('monitor-bug',
     'Permite mostrar el Monitor'),
    ('corte-venta.limite-permitido-revisada',
     '*Limite de desfases permitido para pasar a estado [Revisada] automaticamente'),
    ('alertas.creditos',
     'Alertas relacionadas en Tickets con creditos abiertos a clientes. cambian el color del ícono (punto) a medida que pasan los dias. la metrica es en NUMERO DE DÍAS.'),
    ('notificaciones.qr.tiempo-luego-ya-no-esperar',
     'Tiempo de espera a que se confirme transaccion (correo) luego de cajero hizo click "ya no esperar"'),
    ('tiempo.consulta-notificaciones',
     'Tiempo en minutos en el cual se ejecutara endpoint de consulta del componente de notificaciones')
) AS s(k, ley)
WHERE c.key = s.k
  AND c.leyenda IS DISTINCT FROM s.ley;
