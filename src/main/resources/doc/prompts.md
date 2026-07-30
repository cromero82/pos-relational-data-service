
==============================
los datos se ven coherentes con respecto a la revision anterior, 
ahora inicio sesion para el siguiente turno:
* registro siguientes ventas (Tickets)
  40000 (Efectivo)
  30000 (QR Bancolombia)
  10000 (Nequi)

* Egresos
-  20000 (caja menor)



al ejecutar: origenes de fondos
puedo ver:
Caja: Efectivo
160000 MOVIMIENTOS
+40000 TICKETS SIN CORTE
200000 TOTAL PARCIAL

Bancolombia - QR
0 MOVIMIENTOS
+30000 TICKETS SIN CORTE
30000 TOTAL PARCIAL

Nequi
0 MOVIMIENTOS
+10000 TICKETS SIN CORTE
10000 TOTAL PARCIAL

Caja Menor
40000 TOTAL PARCIAL

Caja General
30000 TOTAL PARCIAL


* Movimientos
de caja:efectivo a: caja menor, valor: 30000

ahora: al actualizarse la vista
Caja: Efectivo
130000 MOVIMIENTOS
+40000 TICKETS SIN CORTE
170000 TOTAL PARCIAL

... Bancolombia - QR y Nequi (igual que antes)

Caja Menor
70000 TOTAL PARCIAL


Caja General
30000 TOTAL PARCIAL


* CIerre de ventas # 2
me muestra todo coherente, a continuacion el HAR:

SALVO por la excepcion del item "metodo de pago": Efectivo: base para proveedores
estoy casi seguro que esos 20 (Egresos sistema) los saque del origen de datos: Caja menor.

de hecho nisiquiera deberia existir ese metodo de pago.

podrias revisarlo hasta este punto (aun no he realizado el cierre)

=====================================

TEMP:
perfecto, ahora.
requiereo que en movimientos, cuando aparezca un tipo: ENTRADA_VENTA, se note con un color de fondo resaltando este registro. y al final en Columna "Detalle" (justo a la derecha (alineacion a derecha) aparezca un voton "Ver" y pueda cargar los detalles de ese corte de ventas.


================

TEMP ( PARA ver el registro resaltado en el historial)
en el app cuya url es: /apps/tickets/historial
es decir "Historial de Ventas" en el listado de items de las ventas: infinito-ai-front/src/app/pages/apps/ventas/historial-ventas/historial-ventas.component.html:50
quiero que ...



====================================
al repetir la prueba todo se comporta igual, pero en funcionalidad: DIstribucion de efectivo, aparece el saldo 250000. la distribucion ahora es: 
base: 160000
caja menor: 60000
caja general: 30000

revisa si los movimientos son correctos, 
Observacion:
- al finalizar distribucion, cierra sesion. al iniciar sesion en el siguiente turno.
- voy directamente a "Origenes de fondos" y en caja:Efectivo :
  - 160000 movimientos
  - 40000 (tickets sin corte)
  - total parcial: 200000
es extraño porque no he vendido nada (que cueste 40000)

=========================
1. MOvimientos paso 1.

excelente, he rgistrado 150000 en movimiento 
INversion inicial:
150000

registro 3 ventas (tickes) en Efectivo
30000
80000
40000

Egreso
- 50000
 origen fondo: Caja efectivo (dice 300000 hasta ese momento)

consulto origenes de fondos:

Caja: Efectivo
100000 MOVIMIENTOS
+150000 TICKETS SIN CORTE
250000 TOTAL PARCIAL

a continuacion cierre de ventas 


y luego se ejecuta inmediatamente : DIstribucion de efectivo.
Tras el cierre, reparte el efectivo de Caja: Efectivo hacia Caja Menor y Caja General. Lo que quede será la Base del próximo turno.

Saldo actual: $100,000
diligencio los inputs:
Base: 60000
Caja menor: 40000
(por defecto caja general: 0)

te adjunto el HAR de las operaciones: CIerre y distribucion:



=============================
he iniciado sesion y no me muestra la funcionalidad distribucion de dinero, obviamente no hay registro alguno.
sugiero que para este escenario (el cual ocurrira en momentos de instalacion de este software), aparezca un modal "ENtrada manual" (la misma que aparece en FInanciero / Origen de fondos click en boton "ENtrada"), donde aparezcan los siguientes campos seteados por defecto:
Origen de fondos:Caja: Efectivo
MOtivo: "Inversion inicial correspondiente a: BASE para caja registradora"
este motivo es especial (solo aparecera en este escenario)
este motivo tiene la misma naturaleza que el utilizado en "Distribucion de dinero", sera utilizado para definir la base del primer corte
=====================================
-- Preguntas de la IA
Ajustes menores al plan (recomiendo)
La distribución genera traslados reales en el ledger (no solo números en un modal).
Esos traslados quedan dentro del watermark del corte que acaba de cerrar (o en un “cierre de ciclo” etiquetado), no en el turno nuevo.
Cajero en distribución: solo Caja: Efectivo si no tiene permiso; admin mueve también a Caja Menor/General.
Electronicos: no pasan por “contar”; base = saldo ledger al momento del corte/confirmación.
Corregir naturaleza de Caja Menor a FISICA.
-------------------------------

1. Si son movimientos
2. los movimientos a ambos origenes (caja menor y caja general) siento que el campo que llamamos "base" vendria siendo el campo "saldo_despues", luego de ese movimiento que vamos a crear. por ende seria utilizado para calcular ese campo base (en futuros cortes de ventas) y los movimientos que contaran (suman o restan) seran los que contaran en el siguiente turno (en el campo movimientos).
3. si cajero no tiene permiso a alguna de las cajas, una vez finaliza la funcionalidad: "COrte de ventas", automaticamente se cierra la sesion. (en tal caso el usuario admin debera iniciar sesion y ejecutar funcionalidad :  Distribución de efectivo)
4. Electronicos no se gestionan con la funcionalidad: Distribucion de efectivo
5. Si es fisica

======================================
centrandonos unicamente en metodo de pago: EFECTIVO (que corresponde a Caja: Efectivo), puedo ver que tiene base:0, MOvimientos: 0. creo que en base al ultimo corte de ventas (anterior) deberia existir la forma de rastrear cual fue el ultimo movimiento con respecto  a ese corte, (o no se si ese desarrollo estaba pendiente en algun sprint de los que planeamos inicialmente) ya que si fuera asi, se detectaria eque en la tabla:
para la consulta:
select id, mof.origen_fondos_id , mof.origen_destino_id , mof.tipo_movimiento , mof.valor , mof.saldo_antes , mof.saldo_despues  
from movimiento_origen_fondos mof order by mof.id desc;
33	1		SALIDA_EGRESO	50000.00	150000.00	100000.00
32	1		ENTRADA_MANUAL	51000.00	99000.00	150000.00
31	2		SALIDA_EGRESO	145000.00	145000.00	0.00
30	2		AJUSTE_CIERRE	1000.00	144000.00	145000.00

y de esta forma AJUSTE_CIERRE rastrearia el ultimo Corte, por lo que considero que en este caso no deberia estar la columna MOvimientos: 0 para "Efectivo", significa que para Caja: arranco con 51.000, al recibir 99000. esto debio aparecer en movimientos (al menos ese valor de 51000). por otro lado, este desfase probablemente se debe a la funcionalidad pendiente: "Setear Caja u Origenes de fondo" (o algo similar, no lo recuerdo, aunque yo prefiero que se llame: Distribución de efectivo) que planeamos inicialmente

estos datos demuestran ciertas tareas pendientes, o vacios, asi que replanteemos:
1. definiciones. en metodos pago / origenes fondo, he renombrado de la siguiente forma para aplicar concepto basico de administracion de fondos.
   - "Caja: Efectivo"  (actualmente existe, caja registradora del establecimiento o pagar de la caja registradora, ok)
   - "Caja Menor" (actualmente id=4, antes nombre: Efectivo: base para proveedores)
   - "Caja general / Fondo administracion" (actualmente id=5, antes nombre: Reserva pago proveedores)
   - he borrado las demas, por lo que la consulta:
   select id, nombre, t.tipo_origen_fondos_id , naturaleza
     from origen_fondos t ;
   genera:
     1	Caja: Efectivo	1	FISICA
     2	Bancolombia - QR	1	ELECTRONICA
     3	Nequi	1	ELECTRONICA
     4	Caja Menor	1	ELECTRONICA
     5	Caja General	2	MIXTA
    
2. A nivel de negocio la dinamica del sistema deberia ser la siguiente
los Metodos de 
a. Usuario inicia sesion.
b. Usuario vende a traves de tickets, hace egresos y puede hacer movimientos. De caja efectivo a caja menor (o a Caja General si tiene permiso), tambien de Caja menor a Caja general si tiene permiso (aunque en estos puntos no deberia haber muchos cambios, la interfaz y funcionalidad de origenes de fondos permite mover entre origenes e fondos a traves de movimientos)
c. Se realiza corte de ventas. 
- con base al registro de ultimo corte, debe poblar los movimientos entre origenes de fondos. por lo que tener presente una marca en cada uno, repasemos, para:
  - para tickes (ventas), existe una columna: corte_venta.ultimo_historial_recibo_id
  - par movimientos, en teoria se haria con: AJUSTE_CIERRE (pero no me queda claro o no lo recuerdo)
  - Base, tampoco me queda claro donde se guarda.
d. una vez se cierra el modal de COrte de ventas, aparece un modal la funcionalidad :  Distribución de efectivo. en la cual el usuario (o cajero si tiene permiso) debera CONTARA con los montos finales del dia y tendra que mover de "Caja: Efectivo" a "Caja menor" y a "Caja General", el saldo final quedara como base.
  - ejemplo si: Caja Efectivo: 2000000, entonces: Base: 150000, mueve 1400000 a "Caja menor", por defecto se completara: 450000 a "Caja General". 
  - para los medios electronicos, la base sera lo que tengan acumulado.
importante: Aqui analizar que tenemos en nuestro desarollo, que movimiento marcara que el ciclo finalizo para que al iniciar nuevamente, el sistema sepa que este ultimo movimiento no entrara los movimientos del corte del siguiente turno (o dia)
e. el sistema mostrara un mensaje "Se cerrara la sesion" con opciones: "Cerrar sesion y Definir luego", "Aceptar"
  - esto es obligar a un cierre de sesion y matar la cookie (tambien con fines estadisticos)
f. puede que usuario omita el paso (d) entonces al inciar sesion nuevamente. esta ventana aparecera nuevamente (es decir la funcionalidad: Definicion Proximo Turno), la opcion "Cancelar", lo enviara al Login nuevamente.

por lo que la funcionalidad de cierre del proximo dia, tendra claro cuanto fue la base de Efectivo: Caja, (los demas medios electronicos que por ahora seran gestionados por el admin) y los movimientos estaran tageados por el ultimo corte, mostraran la base, los ultimos movimientos, y se repetira el ciclo.

===================================
revisemos los movimientos, NOS VAMOS A CENTRAR UNICAMENTE EN LA CAJA: Efectivo
1. he realizado un movimiento para ajustar LA CAJA EN 150000
porque he realizad el corte de ventas id: 87, 

{
"exportedAt": "2026-07-25T22:44:21.990Z",
"user": "Carlos Romero P.",
"route": "/apps/financiero/origenes-fondos",
"userAgent": "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:152.0) Gecko/20100101 Firefox/152.0",
"requests": [
{
"id": 1,
"timestamp": "2026-07-25T22:44:10.051Z",
"method": "GET",
"url": "http://localhost:8088/corte-venta/consultar-rango",
"requestHeaders": {
"Accept": "application/json",
"Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
},
"requestBody": null,
"responseStatus": 200,
"responseStatusText": "OK",
"responseHeaders": {
"cache-control": "no-cache, no-store, max-age=0, must-revalidate",
"content-type": "application/json",
"expires": "0",
"pragma": "no-cache"
},
"responseBody": {
"fechaIni": null,
"fechaFin": "2026-07-25T09:30:15",
"ultimoCorte": "2026-07-25T09:53:20",
"ventasTipo": [],
"total": 0,
"otrosCortesIntersectados": []
},
"durationMs": 957
},
{
"id": 2,
"timestamp": "2026-07-25T22:44:10.051Z",
"method": "GET",
"url": "http://localhost:8088/origenes-fondos/arbol",
"requestHeaders": {
"Accept": "application/json",
"Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
},
"requestBody": null,
"responseStatus": 200,
"responseStatusText": "OK",
"responseHeaders": {
"cache-control": "no-cache, no-store, max-age=0, must-revalidate",
"content-type": "application/json",
"expires": "0",
"pragma": "no-cache"
},
"responseBody": [
{
"id": 1,
"nombre": "Caja: Efectivo",
"nombreDisplay": "Caja: Efectivo",
"nivel": 0,
"parentOrigenFondosId": null,
"metodoPagoId": 1,
"tipoOrigenFondosNombre": "Operativo del día",
"tipoOrigenFondosCodigo": "OPERATIVO",
"esRaiz": true,
"visibleEnEgreso": true,
"color": "#28A745",
"orden": 1,
"saldo": 150000
}
],
"durationMs": 957
},
{
"id": 3,
"timestamp": "2026-07-25T22:44:10.162Z",
"method": "GET",
"url": "http://localhost:8088/movimientos-origen-fondos",
"requestHeaders": {
"Accept": "application/json",
"Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
},
"requestBody": null,
"responseStatus": 200,
"responseStatusText": "OK",
"responseHeaders": {
"cache-control": "no-cache, no-store, max-age=0, must-revalidate",
"content-type": "application/json",
"expires": "0",
"pragma": "no-cache"
},
"responseBody": [
{
"id": 32,
"fecha": "2026-07-25",
"usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
"origenFondosId": 1,
"origenFondosNombre": "Caja: Efectivo",
"origenDestinoId": null,
"origenDestinoNombre": null,
"tipoMovimiento": "ENTRADA_MANUAL",
"valor": 51000,
"impacto": 51000,
"saldoAntes": 99000,
"saldoDespues": 150000,
"metodoPagoId": 1,
"terceroNombre": null,
"motivoMovimientoId": 7,
"motivoMovimientoNombre": "Ajuste con dinero personal del administrador",
"observacion": "para ajustar en 150000",
"valorSistema": null,
"valorReal": null,
"origenTipo": "MANUAL",
"origenId": null,
"grupoTrasladoId": null
},
{
"id": 29,
"fecha": "2026-07-25",
"usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
"origenFondosId": 1,
"origenFondosNombre": "Caja: Efectivo",
"origenDestinoId": null,
"origenDestinoNombre": null,
"tipoMovimiento": "AJUSTE_CIERRE",
"valor": 1000,
"impacto": -1000,
"saldoAntes": 100000,
"saldoDespues": 99000,
"metodoPagoId": 1,
"terceroNombre": null,
"motivoMovimientoId": 10,
"motivoMovimientoNombre": "Error humano: medio de pago equivocado",
"observacion": "Ajuste por cierre #87",
"valorSistema": 121000,
"valorReal": 120000,
"origenTipo": "CIERRE",
"origenId": 87,
"grupoTrasladoId": null
},
{
"id": 25,
"fecha": "2026-07-18",
"usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
"origenFondosId": 1,
"origenFondosNombre": "Caja: Efectivo",
"origenDestinoId": 4,
"origenDestinoNombre": "Efectivo: base para proveedores",
"tipoMovimiento": "TRASLADO",
"valor": 51000,
"impacto": -51000,
"saldoAntes": 151000,
"saldoDespues": 100000,
"metodoPagoId": 1,
"terceroNombre": null,
"motivoMovimientoId": null,
"motivoMovimientoNombre": null,
"observacion": null,
"valorSistema": null,
"valorReal": null,
"origenTipo": "TRASLADO",
"origenId": null,
"grupoTrasladoId": "f9e0bd40-6300-4e23-bbbf-3a5dd3e96840"
},
{
"id": 2,
"fecha": "2026-07-11",
"usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
"origenFondosId": 1,
"origenFondosNombre": "Caja: Efectivo",
"origenDestinoId": null,
"origenDestinoNombre": null,
"tipoMovimiento": "ENTRADA_MANUAL",
"valor": 1000,
"impacto": 1000,
"saldoAntes": 150000,
"saldoDespues": 151000,
"metodoPagoId": 1,
"terceroNombre": null,
"motivoMovimientoId": 8,
"motivoMovimientoNombre": "Otro",
"observacion": "pa completar 200.000",
"valorSistema": null,
"valorReal": null,
"origenTipo": "MANUAL",
"origenId": null,
"grupoTrasladoId": null
},
{
"id": 1,
"fecha": "2026-07-11",
"usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
"origenFondosId": 1,
"origenFondosNombre": "Caja: Efectivo",
"origenDestinoId": null,
"origenDestinoNombre": null,
"tipoMovimiento": "ENTRADA_MANUAL",
"valor": 150000,
"impacto": 150000,
"saldoAntes": 0,
"saldoDespues": 150000,
"metodoPagoId": 1,
"terceroNombre": null,
"motivoMovimientoId": 7,
"motivoMovimientoNombre": "Ajuste con dinero personal del administrador",
"observacion": null,
"valorSistema": null,
"valorReal": null,
"origenTipo": "MANUAL",
"origenId": null,
"grupoTrasladoId": null
}
],
"durationMs": 111
}
]
}

2. Registro una venta a la caja por valor de 100000
   {
   "exportedAt": "2026-07-25T22:48:10.113Z",
   "user": "Carlos Romero P.",
   "route": "/apps/tickets",
   "userAgent": "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:152.0) Gecko/20100101 Firefox/152.0",
   "requests": [
   {
   "id": 1,
   "timestamp": "2026-07-25T22:48:05.302Z",
   "method": "PUT",
   "url": "http://localhost:8088/recibos/6538",
   "requestHeaders": {
   "Content-Type": "application/json",
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": {
   "clienteId": 1,
   "ticketId": 381,
   "estadoId": 2,
   "metodoPagoId": 1,
   "total": "100000.00",
   "sesionId": 63,
   "montoRecibido": 100000
   },
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": {
   "pagado": true,
   "historialReciboId": 6263,
   "documentoVentaId": 6184,
   "documentoVentaConsecutivo": "VTA-006184",
   "total": 100000,
   "fechaCreacion": "2026-07-25T17:48:05",
   "metodoPagoId": 1,
   "clienteId": 1,
   "sesionId": 63
   },
   "durationMs": 127
   },
   {
   "id": 2,
   "timestamp": "2026-07-25T22:48:05.346Z",
   "method": "GET",
   "url": "http://localhost:8088/ticket-recibos/ticket/381?sessionId=63",
   "requestHeaders": {
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": null,
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": {
   "id": 7042,
   "ticketId": 381,
   "reciboId": 6539
   },
   "durationMs": 42
   }
   ]
   }

3. voy a la funcionalidad egresos (apps/financiero/egresos)
realizo una egreso por 50000, pagado con: Efectivo
   {
   "exportedAt": "2026-07-25T22:58:29.269Z",
   "user": "Carlos Romero P.",
   "route": "/apps/financiero/egresos",
   "userAgent": "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:152.0) Gecko/20100101 Firefox/152.0",
   "requests": [
   {
   "id": 2,
   "timestamp": "2026-07-25T22:57:54.990Z",
   "method": "GET",
   "url": "http://localhost:8088/origenes-fondos/arbol-egreso",
   "requestHeaders": {
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": null,
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": [
   {
   "id": 1,
   "nombre": "Caja: Efectivo",
   "nombreDisplay": "Caja: Efectivo",
   "nivel": 0,
   "parentOrigenFondosId": null,
   "metodoPagoId": 1,
   "tipoOrigenFondosNombre": "Operativo del día",
   "tipoOrigenFondosCodigo": "OPERATIVO",
   "esRaiz": true,
   "visibleEnEgreso": true,
   "color": "#28A745",
   "orden": 1,
   "saldo": 150000
   }
   ],
   "durationMs": 122
   },
   {
   "id": 3,
   "timestamp": "2026-07-25T22:57:54.996Z",
   "method": "GET",
   "url": "http://localhost:8088/corte-venta/consultar-rango",
   "requestHeaders": {
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": null,
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": {
   "fechaIni": "2026-07-25T17:48:05",
   "fechaFin": "2026-07-25T17:48:05",
   "ultimoCorte": "2026-07-25T09:53:20",
   "ventasTipo": [
   {
   "metodoPagoId": 1,
   "totalVentasSistema": 100000,
   "totalEgresosSistema": 0,
   "totalSistema": 100000
   },
   {
   "metodoPagoId": 2,
   "totalVentasSistema": 0,
   "totalEgresosSistema": 145000,
   "totalSistema": -145000
   }
   ],
   "total": -45000,
   "otrosCortesIntersectados": []
   },
   "durationMs": 129
   },
   {
   "id": 4,
   "timestamp": "2026-07-25T22:58:24.834Z",
   "method": "POST",
   "url": "http://localhost:8088/egresos",
   "requestHeaders": {
   "Content-Type": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": {
   "fecha": "2026-07-25",
   "valor": 50000,
   "descripcion": "",
   "metodoPagoId": 1,
   "origenFondosId": 1,
   "proveedor": {
   "id": 7
   }
   },
   "responseStatus": 201,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": {
   "id": 17,
   "fecha": "2026-07-25",
   "valor": 50000,
   "descripcion": "",
   "fechaCreacion": null,
   "proveedor": {
   "id": 7,
   "documento": null,
   "nombre": null,
   "telefono": null,
   "correo": null,
   "tipoEgreso": null
   },
   "metodoPagoId": 1,
   "origenFondosId": 1
   },
   "durationMs": 49
   }
   ]
   }
4. voy a la funcionalidad: Origen de fondos
selecciono caja: Efectivo para cargar movimientos.
   {
   "exportedAt": "2026-07-25T23:02:46.647Z",
   "user": "Carlos Romero P.",
   "route": "/apps/financiero/origenes-fondos",
   "userAgent": "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:152.0) Gecko/20100101 Firefox/152.0",
   "requests": [
   {
   "id": 1,
   "timestamp": "2026-07-25T23:02:11.193Z",
   "method": "GET",
   "url": "http://localhost:8088/origenes-fondos/arbol",
   "requestHeaders": {
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": null,
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": [
   {
   "id": 1,
   "nombre": "Caja: Efectivo",
   "nombreDisplay": "Caja: Efectivo",
   "nivel": 0,
   "parentOrigenFondosId": null,
   "metodoPagoId": 1,
   "tipoOrigenFondosNombre": "Operativo del día",
   "tipoOrigenFondosCodigo": "OPERATIVO",
   "esRaiz": true,
   "visibleEnEgreso": true,
   "color": "#28A745",
   "orden": 1,
   "saldo": 100000
   },
   {
   "id": 2,
   "nombre": "Bancolombia - QR",
   "nombreDisplay": "Bancolombia - QR",
   "nivel": 0,
   "parentOrigenFondosId": null,
   "metodoPagoId": 2,
   "tipoOrigenFondosNombre": "Operativo del día",
   "tipoOrigenFondosCodigo": "OPERATIVO",
   "esRaiz": true,
   "visibleEnEgreso": true,
   "color": "#0033A0",
   "orden": 2,
   "saldo": 0
   },
   {
   "id": 6,
   "nombre": "Bolsillo Nómina",
   "nombreDisplay": "──── Bolsillo Nómina",
   "nivel": 1,
   "parentOrigenFondosId": 2,
   "metodoPagoId": 2,
   "tipoOrigenFondosNombre": "Ahorro / nómina",
   "tipoOrigenFondosCodigo": "AHORRO",
   "esRaiz": false,
   "visibleEnEgreso": true,
   "color": null,
   "orden": 201,
   "saldo": 0
   },
   {
   "id": 7,
   "nombre": "Arriendo local",
   "nombreDisplay": "──── Arriendo local",
   "nivel": 1,
   "parentOrigenFondosId": 2,
   "metodoPagoId": 2,
   "tipoOrigenFondosNombre": "Arriendo",
   "tipoOrigenFondosCodigo": "ARRIENDO",
   "esRaiz": false,
   "visibleEnEgreso": true,
   "color": null,
   "orden": 202,
   "saldo": 0
   },
   {
   "id": 3,
   "nombre": "Nequi",
   "nombreDisplay": "Nequi",
   "nivel": 0,
   "parentOrigenFondosId": null,
   "metodoPagoId": 3,
   "tipoOrigenFondosNombre": "Operativo del día",
   "tipoOrigenFondosCodigo": "OPERATIVO",
   "esRaiz": true,
   "visibleEnEgreso": true,
   "color": "#8A2BE2",
   "orden": 3,
   "saldo": 0
   },
   {
   "id": 4,
   "nombre": "Efectivo: base para proveedores",
   "nombreDisplay": "Efectivo: base para proveedores",
   "nivel": 0,
   "parentOrigenFondosId": null,
   "metodoPagoId": 4,
   "tipoOrigenFondosNombre": "Operativo del día",
   "tipoOrigenFondosCodigo": "OPERATIVO",
   "esRaiz": true,
   "visibleEnEgreso": true,
   "color": "#28A745",
   "orden": 4,
   "saldo": 51000
   },
   {
   "id": 5,
   "nombre": "Reserva pago proveedores",
   "nombreDisplay": "Reserva pago proveedores",
   "nivel": 0,
   "parentOrigenFondosId": null,
   "metodoPagoId": null,
   "tipoOrigenFondosNombre": "Reserva proveedores",
   "tipoOrigenFondosCodigo": "RESERVA_PROVEEDORES",
   "esRaiz": true,
   "visibleEnEgreso": false,
   "color": null,
   "orden": 100,
   "saldo": 0
   }
   ],
   "durationMs": 985
   },
   {
   "id": 2,
   "timestamp": "2026-07-25T23:02:11.194Z",
   "method": "GET",
   "url": "http://localhost:8088/corte-venta/consultar-rango",
   "requestHeaders": {
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": null,
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": {
   "fechaIni": "2026-07-25T17:48:05",
   "fechaFin": "2026-07-25T17:48:05",
   "ultimoCorte": "2026-07-25T09:53:20",
   "ventasTipo": [
   {
   "metodoPagoId": 1,
   "totalVentasSistema": 100000,
   "totalEgresosSistema": 50000,
   "totalSistema": 50000
   },
   {
   "metodoPagoId": 2,
   "totalVentasSistema": 0,
   "totalEgresosSistema": 145000,
   "totalSistema": -145000
   }
   ],
   "total": -95000,
   "otrosCortesIntersectados": []
   },
   "durationMs": 984
   },
   {
   "id": 3,
   "timestamp": "2026-07-25T23:02:11.293Z",
   "method": "GET",
   "url": "http://localhost:8088/movimientos-origen-fondos",
   "requestHeaders": {
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": null,
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": [
   {
   "id": 33,
   "fecha": "2026-07-25",
   "usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
   "origenFondosId": 1,
   "origenFondosNombre": "Caja: Efectivo",
   "origenDestinoId": null,
   "origenDestinoNombre": null,
   "tipoMovimiento": "SALIDA_EGRESO",
   "valor": 50000,
   "impacto": -50000,
   "saldoAntes": 150000,
   "saldoDespues": 100000,
   "metodoPagoId": 1,
   "terceroNombre": null,
   "motivoMovimientoId": null,
   "motivoMovimientoNombre": null,
   "observacion": "Egreso #17",
   "valorSistema": null,
   "valorReal": null,
   "origenTipo": "EGRESO",
   "origenId": 17,
   "grupoTrasladoId": null
   }
   ],
   "durationMs": 100
   },

   {
   "id": 4,
   "timestamp": "2026-07-25T23:02:39.510Z",
   "method": "GET",
   "url": "http://localhost:8088/movimientos-origen-fondos",
   "requestHeaders": {
   "Accept": "application/json",
   "Authorization": "Bearer eyJhbGciOiJIU... [truncated]"
   },
   "requestBody": null,
   "responseStatus": 200,
   "responseStatusText": "OK",
   "responseHeaders": {
   "cache-control": "no-cache, no-store, max-age=0, must-revalidate",
   "content-type": "application/json",
   "expires": "0",
   "pragma": "no-cache"
   },
   "responseBody": [
   {
   "id": 33,
   "fecha": "2026-07-25",
   "usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
   "origenFondosId": 1,
   "origenFondosNombre": "Caja: Efectivo",
   "origenDestinoId": null,
   "origenDestinoNombre": null,
   "tipoMovimiento": "SALIDA_EGRESO",
   "valor": 50000,
   "impacto": -50000,
   "saldoAntes": 150000,
   "saldoDespues": 100000,
   "metodoPagoId": 1,
   "terceroNombre": null,
   "motivoMovimientoId": null,
   "motivoMovimientoNombre": null,
   "observacion": "Egreso #17",
   "valorSistema": null,
   "valorReal": null,
   "origenTipo": "EGRESO",
   "origenId": 17,
   "grupoTrasladoId": null
   },
   {
   "id": 32,
   "fecha": "2026-07-25",
   "usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
   "origenFondosId": 1,
   "origenFondosNombre": "Caja: Efectivo",
   "origenDestinoId": null,
   "origenDestinoNombre": null,
   "tipoMovimiento": "ENTRADA_MANUAL",
   "valor": 51000,
   "impacto": 51000,
   "saldoAntes": 99000,
   "saldoDespues": 150000,
   "metodoPagoId": 1,
   "terceroNombre": null,
   "motivoMovimientoId": 7,
   "motivoMovimientoNombre": "Ajuste con dinero personal del administrador",
   "observacion": "para ajustar en 150000",
   "valorSistema": null,
   "valorReal": null,
   "origenTipo": "MANUAL",
   "origenId": null,
   "grupoTrasladoId": null
   },
   {
   "id": 29,
   "fecha": "2026-07-25",
   "usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
   "origenFondosId": 1,
   "origenFondosNombre": "Caja: Efectivo",
   "origenDestinoId": null,
   "origenDestinoNombre": null,
   "tipoMovimiento": "AJUSTE_CIERRE",
   "valor": 1000,
   "impacto": -1000,
   "saldoAntes": 100000,
   "saldoDespues": 99000,
   "metodoPagoId": 1,
   "terceroNombre": null,
   "motivoMovimientoId": 10,
   "motivoMovimientoNombre": "Error humano: medio de pago equivocado",
   "observacion": "Ajuste por cierre #87",
   "valorSistema": 121000,
   "valorReal": 120000,
   "origenTipo": "CIERRE",
   "origenId": 87,
   "grupoTrasladoId": null
   },
   {
   "id": 25,
   "fecha": "2026-07-18",
   "usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
   "origenFondosId": 1,
   "origenFondosNombre": "Caja: Efectivo",
   "origenDestinoId": 4,
   "origenDestinoNombre": "Efectivo: base para proveedores",
   "tipoMovimiento": "TRASLADO",
   "valor": 51000,
   "impacto": -51000,
   "saldoAntes": 151000,
   "saldoDespues": 100000,
   "metodoPagoId": 1,
   "terceroNombre": null,
   "motivoMovimientoId": null,
   "motivoMovimientoNombre": null,
   "observacion": null,
   "valorSistema": null,
   "valorReal": null,
   "origenTipo": "TRASLADO",
   "origenId": null,
   "grupoTrasladoId": "f9e0bd40-6300-4e23-bbbf-3a5dd3e96840"
   },
   {
   "id": 2,
   "fecha": "2026-07-11",
   "usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
   "origenFondosId": 1,
   "origenFondosNombre": "Caja: Efectivo",
   "origenDestinoId": null,
   "origenDestinoNombre": null,
   "tipoMovimiento": "ENTRADA_MANUAL",
   "valor": 1000,
   "impacto": 1000,
   "saldoAntes": 150000,
   "saldoDespues": 151000,
   "metodoPagoId": 1,
   "terceroNombre": null,
   "motivoMovimientoId": 8,
   "motivoMovimientoNombre": "Otro",
   "observacion": "pa completar 200.000",
   "valorSistema": null,
   "valorReal": null,
   "origenTipo": "MANUAL",
   "origenId": null,
   "grupoTrasladoId": null
   },
   {
   "id": 1,
   "fecha": "2026-07-11",
   "usuarioId": "e6606d71-7565-46be-a43c-0b19a4950845",
   "origenFondosId": 1,
   "origenFondosNombre": "Caja: Efectivo",
   "origenDestinoId": null,
   "origenDestinoNombre": null,
   "tipoMovimiento": "ENTRADA_MANUAL",
   "valor": 150000,
   "impacto": 150000,
   "saldoAntes": 0,
   "saldoDespues": 150000,
   "metodoPagoId": 1,
   "terceroNombre": null,
   "motivoMovimientoId": 7,
   "motivoMovimientoNombre": "Ajuste con dinero personal del administrador",
   "observacion": null,
   "valorSistema": null,
   "valorReal": null,
   "origenTipo": "MANUAL",
   "origenId": null,
   "grupoTrasladoId": null
   }
   ],
   "durationMs": 48
   }
   ]
   }

5. 

======================
() se requiere crear un Origen de fondos que por ahora se llama: "Caja General" y he renombrado "Reserva pago proveedores" como "Caja menor" (esto ultimo solo con fines de mantenerte informado, no tenrdas que hacer nada en este punto del renombrado)
=====================================================================
me planteo la inquietud : cuando movimientos base para proveedores.
RTA: Siempre los movimientos del dia. ejemplo: si saque de la caja para base de proveedores, lo hice antes del corte. 
============================================
siguiente bug.
Tabla : historial_recibo tiene 2 columnas misma funcion, por lo que se debe consultar en el backend y en el frontend y dejar solo 1 columna, o aclarame segun los sprint anteriores, si ambas columnas son necesarias para el control de movimientos (ingresos, egresos, movimientos)?

==============================================
Al intentar hacer egreso (en la funcionalidad: /apps/financiero/egresos), necesito que en el campo campo: origene de fondo, los elementos de la lista muestra "Total parcial" que calculaste en cada uno de los origenes de fondo  que calculaste en la funcionalidad: apps/financiero/origenes-fondos
=================================
he ingresado a la tabla: motivo_movimiento el siguiente registro:
15	OTRO_INGRESO	Ingreso o Consignación personal / No del negocio	AJUSTE	true	true	71

adicionalmente, 
=================================
## logica del MigrationService gestion de conflictos en registros:
```powershel
dentro del objeto MigrationResult Incluye Un campo llamado conflicto Que es un arreglo de datos  Que tendrá Dos campos 1 llamado Campo referencia  Y otro campo Conflicto  Y allí poblarás  los siguientes escenarios De los productos que se han cargado :
Productos con mismo nombre Pero código de barras YO precio distinto  Ejemplo :
codigo barras: WINNY
nombre: WINNY ETAPA 4
precio: $1,800.00
y Este otro registro :
codigo barras: 7701021114136
nombre: WINNY ETAPA 4
precio: $1,600.00

Para este ejemplo El valor Del item conflicto debería ser:
[
{
referencia: "2 productos con nombres iguales",
conflicto[
{codigo barras: "7701021114136", codigo barras 2nd: "WINNY"},
{precio: "$1,800.00", precio 2nd: "$1,600.00"}]
},..
]
El caso anterior Puede que el precio sea igual En tal caso Ese es su registro de precio no no estaría allí Sería omitido

Y otro escenario Es donde código de barras sin nombre son iguales (Esto no aplica para un registro versus otro esto aplica Como un análisis de cada registro en sí ) Para lo cual El registro de conflictos Debería ser
ejemplo datos:
codigo barras:PANELITA
nombre:PANELITA
precio: 1100.00
Con lo que el registro de conflictos es Para este ejemplo
[
{
referencia: "Nombre y codigo de barras iguales",
conflicto[
{nombre: "PANELITA"},
},..
]
```