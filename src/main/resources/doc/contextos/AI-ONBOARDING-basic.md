# AI Onboarding - Sistema POS Infinito
### Version 2 — Abril 2026

Documento de contexto para una IA que vaya a trabajar en cualquiera de los
repos del **Sistema POS Infinito** 

## 1. TL;DR
- Sistema POS para tiendas de abarrotes colombianas, distribucion **local**
  (sin nube). Stack OSS para evitar lios de licencia.
- Compuesto por **5 repos** (ms negocio, ms seguridad, ms smtp, frontend Angular, app lanzadora JavaFX)
- La aplicacion actual corresponde a: ms negocio ( Spring Boot /home/carlosr/Documentos/dev/repos/pos-relational-data-service)
- Funcionalidades:
  (la carpeta de controladores se encuentra en: /home/carlosr/Documentos/dev/repos/pos-relational-data-service/src/main/java/com/infinitesoft/pos_relational_data_service/controllers)
  - Productos. HistorialProductoController.java
    - Cargue masivos de productos ( CargueProductoConflictoController.java, CargueProductoController.java)
    - Agrupamiento de productos (GrupoEspejoController.java)
  - Ventas y Facturacion
    - Metodos de Pago. MetodoPagoController.java
    - Ticket de venta (TIcket y Ticket Recibo) EstadoReciboController.java, TicketController.java, TicketReciboController.java
    - Recibo y Recibo detalle: ReciboController.java, ReciboDetalleController.java, EstadoReciboController.java, EdicionReciboController.java, EdicionReciboDetalleController.java
    - Datos historiricos de ventas: HistorialReciboController.java, HistorialReciboDetalleController.java
  - Analisis financiero:
    - Egresos: EgresoController.java
    - Estaditicas Financieras: Ingresos por dia, mes o año, tambien calculo de utilidad. EstadisticaFinancieraController.java
    - Corte de ventas (Ingresos): CorteVentaController.java
  - Clientes: Usuarios y Sesiones (EL cual utiliza el ms seguridad)
  - Actividades de gestion de datos:
    - Cargue de producto: CargueProductoController.java, CargueProductoConflictoController.java
    - Copias de Seguridad (CopiasSeguridadController.java)
    - Monitorizacion de ventas: BitacoraUsuario (Funciona de manera transversal en la implementacion de varios servicios)
      - Evento. EventoController.java
      - BitacoraUsuario: BitacoraUsuarioController.java (y servicios internos, como BitacoraUsuarioService.java)
  - Seguridad
    - Autenticacion: AuthController.java, y los recursos en: /home/carlosr/Documentos/dev/repos/pos-relational-data-service/src/main/java/com/infinitesoft/pos_relational_data_service/security, especialmente: 
      - [AuthClient.java](../../../java/com/infinitesoft/pos_relational_data_service/security/client/AuthClient.java)
      - [UsuarioClient.java](../../../java/com/infinitesoft/pos_relational_data_service/security/client/UsuarioClient.java)
      - [TokenAuthFilter.java](../../../java/com/infinitesoft/pos_relational_data_service/security/filter/TokenAuthFilter.java)
      - [AuthValidationServiceImpl.java](../../../java/com/infinitesoft/pos_relational_data_service/security/service/impl/AuthValidationServiceImpl.java)
      - [SecurityContextHelper.java](../../../java/com/infinitesoft/pos_relational_data_service/security/util/SecurityContextHelper.java)

## Tareas programadas.
  - actualmente no existen como tal, pero si la filosofia de ejecucion. Y sucede cuando un usuario inicia sesion, se intenta ejecutar 
      un proceso de actualizacion de estadisticas del dia anterior llamado: "ejecutar cierre", puede verse en:com.infinitesoft.pos_relational_data_service.controllers.SesionController.create
        lo que realiza es buscar desde la fecha anterior los egresos, ingresos y calcular utilidad, esto lo hace por dia (anterior), mes (anterior), año (anterior), y luego el anterior del anterior, hasta que encuentre un registro estadistico financiero.