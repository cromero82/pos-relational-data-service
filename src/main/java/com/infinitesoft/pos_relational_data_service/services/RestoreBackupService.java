package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.RestoreBackupResponseDto;
import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.repositories.*;
import com.infinitesoft.pos_relational_data_service.security.client.AuthClient;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthBackupDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RestoreBackupService {

    private static final Logger log = LoggerFactory.getLogger(RestoreBackupService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Repositorios
    private final ClientRepository clientRepository;
    private final EstadoReciboRepository estadoReciboRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final CompanyRepository companyRepository;
    private final ConfiguracionAppRepository configuracionAppRepository;
    private final EventoRepository eventoRepository;
    private final ProductRepository productRepository;
    private final HistorialProductoRepository historialProductoRepository;
    private final SesionRepository sesionRepository;
    private final TicketRepository ticketRepository;
    private final TicketReciboRepository ticketReciboRepository;
    private final ReciboRepository reciboRepository;
    private final HistorialReciboRepository historialReciboRepository;
    private final ReciboDetalleRepository reciboDetalleRepository;
    private final HistorialReciboDetalleRepository historialReciboDetalleRepository;
    private final ReciboDetalleHistoricoRepository reciboDetalleHistoricoRepository;
    private final EdicionReciboRepository edicionReciboRepository;
    private final EdicionReciboDetalleRepository edicionReciboDetalleRepository;
    private final TipoEgresoRepository tipoEgresoRepository;
    private final ProveedorRepository proveedorRepository;
    private final EgresoRepository egresoRepository;
    private final FlujoDineroRepository flujoDineroRepository;
    private final TipoResultadoFinRepository tipoResultadoFinRepository;
    private final EstadisticaFinRepository estadisticaFinRepository;
    private final CorteVentaRepository corteVentaRepository;
    private final VentasTipoRepository ventasTipoRepository;
    private final BitacoraUsuarioRepository bitacoraUsuarioRepository;
    private final CargueProductoRepository cargueProductoRepository;
    private final CargueProductoConflictoRepository cargueProductoConflictoRepository;
    private final GrupoEspejoRepository grupoEspejoRepository;

    private final AuthClient authClient;

    @Transactional
    public RestoreBackupResponseDto restaurarBackup(MultipartFile file, String tipo) throws IOException {
        log.info("[RestoreBackupService] Iniciando restauración de backup con tipo: {}", tipo);

        RestoreBackupResponseDto.RestoreBackupResponseDtoBuilder response = RestoreBackupResponseDto.builder();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            // 1. Restaurar datos de autenticación (si existen las hojas)
            AuthBackupDto authBackupDto = extraerDatosAutenticacion(workbook);
            if (authBackupDto != null) {
                Map<String, Integer> authResult = authClient.restaurarBackup(authBackupDto);
                response.rolesCreados(authResult.getOrDefault("rolesCreados", 0));
                response.rolesActualizados(authResult.getOrDefault("rolesActualizados", 0));
                response.usuariosCreados(authResult.getOrDefault("usuariosCreados", 0));
                response.usuariosActualizados(authResult.getOrDefault("usuariosActualizados", 0));
                response.perfilesCreados(authResult.getOrDefault("perfilesCreados", 0));
                response.perfilesActualizados(authResult.getOrDefault("perfilesActualizados", 0));
            }

            // 2. Restaurar tablas sin relaciones
            response.eventosCreados(0).eventosActualizados(0);
            response.estadoRecibosCreados(0).estadoRecibosActualizados(0);
            response.metodoPagoCreados(0).metodoPagoActualizados(0);
            response.configuracionAppCreados(0).configuracionAppActualizados(0);
            response.clientesCreados(0).clientesActualizados(0);
            response.companyCreados(0).companyActualizados(0);

            restaurarTablasSinRelaciones(workbook, tipo, response);

            // 3. Restaurar tablas con relaciones (en orden de dependencias)
            restaurarTablasConRelaciones(workbook, tipo, response);

            return response
                    .exito(true)
                    .mensaje("Backup restaurado exitosamente")
                    .build();

        } catch (Exception e) {
            log.error("[RestoreBackupService] Error restaurando backup: {}", e.getMessage(), e);
            return RestoreBackupResponseDto.builder()
                    .exito(false)
                    .mensaje("Error al restaurar backup: " + e.getMessage())
                    .build();
        }
    }

    private AuthBackupDto extraerDatosAutenticacion(Workbook workbook) {
        // Esta función extraerá los datos de las hojas Usuarios, Roles y Usuario Perfiles
        // y los convertirá al formato AuthBackupDto para enviar al servicio de autenticación
        // Por ahora retornamos null y lo implementaremos después
        log.info("[RestoreBackupService] Extracción de datos de autenticación - no implementado aún");
        return null;
    }

    private void restaurarTablasSinRelaciones(Workbook workbook, String tipo, RestoreBackupResponseDto.RestoreBackupResponseDtoBuilder response) {
        // Restaurar eventos
        int[] eventosStats = restaurarEventos(workbook, tipo);
        response.eventosCreados(eventosStats[0]).eventosActualizados(eventosStats[1]);

        // Restaurar estado recibos
        int[] estadoRecibosStats = restaurarEstadoRecibos(workbook, tipo);
        response.estadoRecibosCreados(estadoRecibosStats[0]).estadoRecibosActualizados(estadoRecibosStats[1]);

        // Restaurar método pago
        int[] metodoPagoStats = restaurarMetodoPago(workbook, tipo);
        response.metodoPagoCreados(metodoPagoStats[0]).metodoPagoActualizados(metodoPagoStats[1]);

        // Restaurar configuración app
        int[] configuracionAppStats = restaurarConfiguracionApp(workbook, tipo);
        response.configuracionAppCreados(configuracionAppStats[0]).configuracionAppActualizados(configuracionAppStats[1]);

        // Restaurar clientes
        int[] clientesStats = restaurarClientes(workbook, tipo);
        response.clientesCreados(clientesStats[0]).clientesActualizados(clientesStats[1]);

        // Restaurar company
        int[] companyStats = restaurarCompany(workbook, tipo);
        response.companyCreados(companyStats[0]).companyActualizados(companyStats[1]);
    }

    private void restaurarTablasConRelaciones(Workbook workbook, String tipo, RestoreBackupResponseDto.RestoreBackupResponseDtoBuilder response) {
        // Orden de dependencias basado en las relaciones de las tablas

        // 1. Productos (no depende de nada en este contexto)
        int[] productosStats = restaurarProductos(workbook, tipo);
        response.productosCreados(productosStats[0]).productosActualizados(productosStats[1]);

        // 2. Historial productos (depende de productos y eventos)
        int[] historialProductosStats = restaurarHistorialProductos(workbook, tipo);
        response.historialProductosCreados(historialProductosStats[0]).historialProductosActualizados(historialProductosStats[1]);

        // 3. Sesiones
        int[] sesionesStats = restaurarSesiones(workbook, tipo);
        response.sesionesCreadas(sesionesStats[0]).sesionesActualizadas(sesionesStats[1]);

        // 4. Tickets (depende de sesiones)
        int[] ticketsStats = restaurarTickets(workbook, tipo);
        response.ticketsCreados(ticketsStats[0]).ticketsActualizados(ticketsStats[1]);

        // 5. Recibos (depende de clientes, estado_recibos, metodo_pago, sesiones)
        int[] recibosStats = restaurarRecibos(workbook, tipo);
        response.recibosCreados(recibosStats[0]).recibosActualizados(recibosStats[1]);

        // 6. Ticket Recibos (depende de tickets y recibos)
        int[] ticketRecibosStats = restaurarTicketRecibos(workbook, tipo);
        response.ticketRecibosCreados(ticketRecibosStats[0]).ticketRecibosActualizados(ticketRecibosStats[1]);

        // 7. Historial Recibos
        int[] historialRecibosStats = restaurarHistorialRecibos(workbook, tipo);
        response.historialRecibosCreados(historialRecibosStats[0]).historialRecibosActualizados(historialRecibosStats[1]);

        // 8. Recibo Detalles (depende de recibos y productos)
        int[] reciboDetallesStats = restaurarReciboDetalles(workbook, tipo);
        response.reciboDetallesCreados(reciboDetallesStats[0]).reciboDetallesActualizados(reciboDetallesStats[1]);

        // 9. Historial Recibo Detalles (depende de historial_recibo)
        int[] historialReciboDetallesStats = restaurarHistorialReciboDetalles(workbook, tipo);
        response.historialReciboDetallesCreados(historialReciboDetallesStats[0]).historialReciboDetallesActualizados(historialReciboDetallesStats[1]);

        // 10. Recibo Detalle Históricos
        int[] reciboDetalleHistoricosStats = restaurarReciboDetalleHistoricos(workbook, tipo);
        response.reciboDetalleHistoricosCreados(reciboDetalleHistoricosStats[0]).reciboDetalleHistoricosActualizados(reciboDetalleHistoricosStats[1]);

        // 11. Edición Recibos
        int[] edicionRecibosStats = restaurarEdicionRecibos(workbook, tipo);
        response.edicionRecibosCreados(edicionRecibosStats[0]).edicionRecibosActualizados(edicionRecibosStats[1]);

        // 12. Edición Recibo Detalles (depende de edicion_recibo)
        int[] edicionReciboDetallesStats = restaurarEdicionReciboDetalles(workbook, tipo);
        response.edicionReciboDetallesCreados(edicionReciboDetallesStats[0]).edicionReciboDetallesActualizados(edicionReciboDetallesStats[1]);

        // 13. Tipo Egresos
        int[] tipoEgresosStats = restaurarTipoEgresos(workbook, tipo);
        response.tipoEgresosCreados(tipoEgresosStats[0]).tipoEgresosActualizados(tipoEgresosStats[1]);

        // 14. Proveedores (depende de tipo_egreso)
        int[] proveedoresStats = restaurarProveedores(workbook, tipo);
        response.proveedoresCreados(proveedoresStats[0]).proveedoresActualizados(proveedoresStats[1]);

        // 15. Egresos (depende de proveedor)
        int[] egresosStats = restaurarEgresos(workbook, tipo);
        response.egresosCreados(egresosStats[0]).egresosActualizados(egresosStats[1]);

        // 16. Flujo Dinero
        int[] flujoDineroStats = restaurarFlujoDinero(workbook, tipo);
        response.flujoDineroCreados(flujoDineroStats[0]).flujoDineroActualizados(flujoDineroStats[1]);

        // 17. Tipo Resultado Fin
        int[] tipoResultadoFinStats = restaurarTipoResultadoFin(workbook, tipo);
        response.tipoResultadoFinCreados(tipoResultadoFinStats[0]).tipoResultadoFinActualizados(tipoResultadoFinStats[1]);

        // 18. Estadística Fin (depende de tipo_resultado_fin)
        int[] estadisticaFinStats = restaurarEstadisticaFin(workbook, tipo);
        response.estadisticaFinCreados(estadisticaFinStats[0]).estadisticaFinActualizados(estadisticaFinStats[1]);

        // 19. Corte Ventas
        int[] corteVentasStats = restaurarCorteVentas(workbook, tipo);
        response.corteVentasCreados(corteVentasStats[0]).corteVentasActualizados(corteVentasStats[1]);

        // 20. Ventas Tipo (depende de corte_venta)
        int[] ventasTipoStats = restaurarVentasTipo(workbook, tipo);
        response.ventasTipoCreados(ventasTipoStats[0]).ventasTipoActualizados(ventasTipoStats[1]);

        // 21. Bitácora Usuario (depende de eventos)
        int[] bitacoraUsuariosStats = restaurarBitacoraUsuarios(workbook, tipo);
        response.bitacoraUsuariosCreados(bitacoraUsuariosStats[0]).bitacoraUsuariosActualizados(bitacoraUsuariosStats[1]);

        // 22. Cargue Productos
        int[] cargueProductosStats = restaurarCargueProductos(workbook, tipo);
        response.cargueProductosCreados(cargueProductosStats[0]).cargueProductosActualizados(cargueProductosStats[1]);

        // 23. Cargue Producto Conflictos (depende de cargue_productos)
        int[] cargueProductoConflictosStats = restaurarCargueProductoConflictos(workbook, tipo);
        response.cargueProductoConflictosCreados(cargueProductoConflictosStats[0]).cargueProductoConflictosActualizados(cargueProductoConflictosStats[1]);

        // 24. Grupo Espejo (depende de producto)
        int[] grupoEspejoStats = restaurarGrupoEspejo(workbook, tipo);
        response.grupoEspejoCreados(grupoEspejoStats[0]).grupoEspejoActualizados(grupoEspejoStats[1]);
    }

    // =====================================================================
    // Métodos auxiliares para restaurar cada tabla
    // =====================================================================

    private int[] restaurarEventos(Workbook workbook, String tipo) {
        Sheet sheet = workbook.getSheet("evento");
        if (sheet == null) return new int[]{0, 0};

        int creados = 0, actualizados = 0;

        if ("full-reescritura".equals(tipo)) {
            eventoRepository.deleteAll();
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Integer id = getCellValueAsInteger(row.getCell(0));
            String nombre = getCellValueAsString(row.getCell(1));
            String sigla = getCellValueAsString(row.getCell(2));

            if ("incremental".equals(tipo)) {
                if (eventoRepository.existsById(id)) {
                    continue;
                }
            }

            Evento evento = eventoRepository.findById(id).orElse(new Evento());
            boolean esNuevo = evento.getId() == null;

            evento.setId(id);
            evento.setNombre(nombre);
            evento.setSigla(sigla);

            eventoRepository.save(evento);

            if (esNuevo) creados++;
            else actualizados++;
        }

        return new int[]{creados, actualizados};
    }

    private int[] restaurarEstadoRecibos(Workbook workbook, String tipo) {
        Sheet sheet = workbook.getSheet("Estado Recibos");
        if (sheet == null) return new int[]{0, 0};

        int creados = 0, actualizados = 0;

        if ("full-reescritura".equals(tipo)) {
            estadoReciboRepository.deleteAll();
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long id = getCellValueAsLong(row.getCell(0));
            String descripcion = getCellValueAsString(row.getCell(1));
            String sigla = getCellValueAsString(row.getCell(2));

            if ("incremental".equals(tipo)) {
                if (estadoReciboRepository.existsById(id)) {
                    continue;
                }
            }

            EstadoRecibo estadoRecibo = estadoReciboRepository.findById(id).orElse(new EstadoRecibo());
            boolean esNuevo = estadoRecibo.getId() == null;

            estadoRecibo.setId(id);
            estadoRecibo.setDescripcion(descripcion);
            estadoRecibo.setSigla(sigla);

            estadoReciboRepository.save(estadoRecibo);

            if (esNuevo) creados++;
            else actualizados++;
        }

        return new int[]{creados, actualizados};
    }

    private int[] restaurarMetodoPago(Workbook workbook, String tipo) {
        Sheet sheet = workbook.getSheet("Metodo Pago");
        if (sheet == null) return new int[]{0, 0};

        int creados = 0, actualizados = 0;

        if ("full-reescritura".equals(tipo)) {
            metodoPagoRepository.deleteAll();
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long id = getCellValueAsLong(row.getCell(0));
            String descripcion = getCellValueAsString(row.getCell(1));
            String estado = getCellValueAsString(row.getCell(2));
            String file = getCellValueAsString(row.getCell(3));
            String sigla = getCellValueAsString(row.getCell(4));
            String color = getCellValueAsString(row.getCell(5));

            if ("incremental".equals(tipo)) {
                if (metodoPagoRepository.existsById(id)) {
                    continue;
                }
            }

            MetodoPago metodoPago = metodoPagoRepository.findById(id).orElse(new MetodoPago());
            boolean esNuevo = metodoPago.getId() == null;

            metodoPago.setId(id);
            metodoPago.setDescripcion(descripcion);
            metodoPago.setEstado(estado);
            metodoPago.setFile(file);
            metodoPago.setSigla(sigla);
            metodoPago.setColor(color);

            metodoPagoRepository.save(metodoPago);

            if (esNuevo) creados++;
            else actualizados++;
        }

        return new int[]{creados, actualizados};
    }

    private int[] restaurarConfiguracionApp(Workbook workbook, String tipo) {
        Sheet sheet = workbook.getSheet("Configuracion App");
        if (sheet == null) return new int[]{0, 0};

        int creados = 0, actualizados = 0;

        if ("full-reescritura".equals(tipo)) {
            configuracionAppRepository.deleteAll();
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long id = getCellValueAsLong(row.getCell(0));
            String key = getCellValueAsString(row.getCell(1));
            String value = getCellValueAsString(row.getCell(2));

            if ("incremental".equals(tipo)) {
                if (configuracionAppRepository.existsById(id)) {
                    continue;
                }
            }

            ConfiguracionApp config = configuracionAppRepository.findById(id).orElse(new ConfiguracionApp());
            boolean esNuevo = config.getId() == null;

            config.setId(id);
            config.setKey(key);
            config.setValue(value);

            configuracionAppRepository.save(config);

            if (esNuevo) creados++;
            else actualizados++;
        }

        return new int[]{creados, actualizados};
    }

    private int[] restaurarClientes(Workbook workbook, String tipo) {
        Sheet sheet = workbook.getSheet("Clientes");
        if (sheet == null) return new int[]{0, 0};

        int creados = 0, actualizados = 0;

        if ("full-reescritura".equals(tipo)) {
            clientRepository.deleteAll();
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long id = getCellValueAsLong(row.getCell(0));
            String nombre = getCellValueAsString(row.getCell(1));
            String telefono = getCellValueAsString(row.getCell(2));
            String documento = getCellValueAsString(row.getCell(3));

            if ("incremental".equals(tipo)) {
                if (clientRepository.existsById(id)) {
                    continue;
                }
            }

            Client client = clientRepository.findById(id).orElse(new Client());
            boolean esNuevo = client.getId() == null;

            client.setId(id);
            client.setNombre(nombre);
            client.setTelefono(telefono);
            client.setDocumento(documento);

            clientRepository.save(client);

            if (esNuevo) creados++;
            else actualizados++;
        }

        return new int[]{creados, actualizados};
    }

    private int[] restaurarCompany(Workbook workbook, String tipo) {
        Sheet sheet = workbook.getSheet("Company");
        if (sheet == null) return new int[]{0, 0};

        int creados = 0, actualizados = 0;

        if ("full-reescritura".equals(tipo)) {
            companyRepository.deleteAll();
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Long id = getCellValueAsLong(row.getCell(0));
            String nombre = getCellValueAsString(row.getCell(1));
            String descripcion = getCellValueAsString(row.getCell(2));
            String email = getCellValueAsString(row.getCell(3));
            String telefono = getCellValueAsString(row.getCell(4));
            String contactName = getCellValueAsString(row.getCell(5));

            if ("incremental".equals(tipo)) {
                if (companyRepository.existsById(id)) {
                    continue;
                }
            }

            Company company = companyRepository.findById(id).orElse(new Company());
            boolean esNuevo = company.getId() == null;

            company.setId(id);
            company.setName(nombre);
            company.setDescription(descripcion);
            company.setEmail(email);
            company.setTelefono(telefono);
            company.setContactName(contactName);

            companyRepository.save(company);

            if (esNuevo) creados++;
            else actualizados++;
        }

        return new int[]{creados, actualizados};
    }

    // Métodos stub para las demás tablas (se implementarán según sea necesario)
    private int[] restaurarProductos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarHistorialProductos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarSesiones(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarTickets(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarRecibos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarTicketRecibos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarHistorialRecibos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarReciboDetalles(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarHistorialReciboDetalles(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarReciboDetalleHistoricos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarEdicionRecibos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarEdicionReciboDetalles(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarTipoEgresos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarProveedores(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarEgresos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarFlujoDinero(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarTipoResultadoFin(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarEstadisticaFin(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarCorteVentas(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarVentasTipo(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarBitacoraUsuarios(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarCargueProductos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarCargueProductoConflictos(Workbook workbook, String tipo) { return new int[]{0, 0}; }
    private int[] restaurarGrupoEspejo(Workbook workbook, String tipo) { return new int[]{0, 0}; }

    // =====================================================================
    // Métodos utilitarios
    // =====================================================================

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private Long getCellValueAsLong(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                try {
                    return Long.parseLong(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        Double value = getCellValueAsDouble(cell);
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private LocalDateTime getCellValueAsLocalDateTime(Cell cell) {
        String value = getCellValueAsString(cell);
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate getCellValueAsLocalDate(Cell cell) {
        String value = getCellValueAsString(cell);
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private UUID getCellValueAsUUID(Cell cell) {
        String value = getCellValueAsString(cell);
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim().toUpperCase();
                return "SI".equals(value) || "TRUE".equals(value) || "1".equals(value);
            case NUMERIC:
                return cell.getNumericCellValue() != 0;
            default:
                return null;
        }
    }
}
