* Backend ms 
continuando en modo plan, ahora es importante definir el microservicio que se encargara del tunel,
ok entonces enfoquemonos en el backend, para ello se ha dispuesto el siguiente repositorio: /Users/carlosromero/Documents/dev/repos/puente-tienda alli se debe generar un ms con acceso a bd postgres misma bd controlneg_rmx_db pero genera un nuevo esquema con unas tablas suficientes para registrar.

================================


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


* 2. Movimientos
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


========================
otro aspecto visual es en  la funcionalidad : Origenes de fondos.
vamos a optimizar los bolsillos "hijos":
vamos a hacerlos un poco mas pequeños (las fuentes y el alto, la idea es disminuir espacio , ejemplo el espacio entre el borde  superior e inferior con la primer y ultimo texto disminuirlo tambien) me agrada que sean verticales. pregunto para que es esa linea que se ve: _______
?
en Origenes de fondos hijos no vamos a mostrar resumen de movimientos (visualmente), para estos casos el usuario tendra la tabla.
te adjunto imagen de verticalmente como se ve el Origen de fondo padre y los 2 hijos que tiene el : Bancolombia - QR

en pantallas verticalmente pequeñas no es posible ver la tabla de movimientos. y una razon es los origenes de fondos que son hijos, estan ocupando un algo espacio.  y luego vemos para que la tabla


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

Saldo actual: 250000
diligencio los inputs:
Base: 60000
Caja menor: 40000
(por defecto caja general: 0)

te adjunto el HAR de las operaciones: CIerre y distribucion:`