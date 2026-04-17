package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.repositories.*;
import com.infinitesoft.pos_relational_data_service.security.client.AuthClient;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    // ── Servicios existentes ──────────────────────────────────────────────────
    private final ProductService productService;
    private final HistorialProductoService historialProductoService;
    private final HistorialReciboService historialReciboService;
    private final CorteVentaService corteVentaService;
    private final VentasTipoService ventasTipoService;
    private final EventoService eventoService;
    private final BitacoraUsuarioService bitacoraUsuarioService;
    private final AuthClient authClient;
    private final EmailService emailService;

    // ── Repositorios existentes ───────────────────────────────────────────────
    private final HistorialReciboDetalleRepository historialReciboDetalleRepository;
    private final ConfiguracionAppRepository configuracionAppRepository;
    private final EstadoReciboRepository estadoReciboRepository;

    // ── Repositorios nuevos ───────────────────────────────────────────────────
    private final ClientRepository clientRepository;
    private final CompanyRepository companyRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final ReciboRepository reciboRepository;
    private final ReciboDetalleRepository reciboDetalleRepository;
    private final ReciboDetalleHistoricoRepository reciboDetalleHistoricoRepository;
    private final EdicionReciboRepository edicionReciboRepository;
    private final EdicionReciboDetalleRepository edicionReciboDetalleRepository;
    private final EgresoRepository egresoRepository;
    private final ProveedorRepository proveedorRepository;
    private final TipoEgresoRepository tipoEgresoRepository;
    private final SesionRepository sesionRepository;
    private final TicketRepository ticketRepository;
    private final TicketReciboRepository ticketReciboRepository;
    private final FlujoDineroRepository flujoDineroRepository;
    private final EstadisticaFinRepository estadisticaFinRepository;
    private final TipoResultadoFinRepository tipoResultadoFinRepository;
    private final CargueProductoRepository cargueProductoRepository;
    private final CargueProductoConflictoRepository cargueProductoConflictoRepository;

    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarBackupExcel(String token) throws IOException {
        log.info("[BackupService] Iniciando generación de backup Excel");
        try (Workbook workbook = new XSSFWorkbook()) {

            // ── Hojas existentes ──────────────────────────────────────────────
            crearHojaProductos(workbook);
            crearHojaHistorialProductos(workbook);
            crearHojaHistorialRecibo(workbook);
            crearHojaHistorialReciboDetalle(workbook);
            crearHojaCorteVenta(workbook);
            crearHojaVentasTipo(workbook);
            crearHojaEvento(workbook);
            crearHojaBitacoraUsuario(workbook);
            crearHojaUsuarios(workbook, token);
            crearHojaConfiguracionApp(workbook);
            crearHojaEstadoRecibos(workbook);

            // ── Hojas nuevas ──────────────────────────────────────────────────
            crearHojaClients(workbook);
            crearHojaCompany(workbook);
            crearHojaMetodoPago(workbook);
            crearHojaRecibo(workbook);
            crearHojaReciboDetalle(workbook);
            crearHojaReciboDetalleHistorico(workbook);
            crearHojaEdicionRecibo(workbook);
            crearHojaEdicionReciboDetalle(workbook);
            crearHojaEgreso(workbook);
            crearHojaProveedor(workbook);
            crearHojaTipoEgreso(workbook);
            crearHojaSesion(workbook);
            crearHojaTicket(workbook);
            crearHojaTicketRecibo(workbook);
            crearHojaFlujoDinero(workbook);
            crearHojaEstadisticaFin(workbook);
            crearHojaTipoResultadoFin(workbook);
            crearHojaCargueProductos(workbook);
            crearHojaCargueProductoConflictos(workbook);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void enviarBackupPorCorreo(String token, String correoDestino) throws Exception {
        byte[] backupBytes = generarBackupExcel(token);
        String fileName = "Backup Gestor Market " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss")) + ".xlsx";

        String html = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 20px;'>" +
                "<h2 style='color: #2c3e50; text-align: center;'>Copia de Seguridad - Gestor Market</h2>" +
                "<p style='font-size:16px; color:#333;'>Hola,</p>" +
                "<p style='font-size:16px; color:#333;'>Se adjunta la copia de seguridad generada automáticamente.</p>" +
                "<div style='background-color:#f9f9f9; padding:15px; border-radius:5px; margin: 20px 0;'>" +
                "<p style='margin:0; font-weight:bold;'>Archivo: " + fileName + "</p>" +
                "<p style='margin:5px 0 0 0;'>Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>" +
                "</div>" +
                "<hr style='margin:20px 0; border:none; border-top:1px solid #ddd;'/>" +
                "<p style='font-size:12px; color:#999; text-align:center;'>Este mensaje fue generado automáticamente por el sistema de Gestión de Mercado.</p>" +
                "</div>";

        emailService.enviarCorreoConAdjunto(correoDestino, "Copia de Seguridad - Gestor Market", html, backupBytes, fileName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HOJAS EXISTENTES (actualizadas)
    // ─────────────────────────────────────────────────────────────────────────

    private void crearHojaProductos(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Productos");
        // Se agregó "Precio Unidad" que faltaba (campo precio_unidad en DB y JPA)
        String[] headers = {
                "ID", "Código Barras", "Nombre", "Precio", "Precio Compra",
                "Precio Unidad", "Fecha Actualización Precio", "Activo",
                "Fecha Creación", "Total Ventas", "Fecha Última Venta", "Porcentaje Ganancia"
        };
        createHeaderRow(sheet, headers);

        List<Product> list = productService.findAll();
        int rowIdx = 1;
        for (Product item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getBarcode()));
            row.createCell(2).setCellValue(clean(item.getNombre()));
            row.createCell(3).setCellValue(item.getPrecio() != null ? item.getPrecio() : 0.0);
            row.createCell(4).setCellValue(item.getPrecioCompra() != null ? item.getPrecioCompra() : 0.0);
            row.createCell(5).setCellValue(item.getPrecioUnidad() != null ? item.getPrecioUnidad() : 0.0);
            row.createCell(6).setCellValue(formatDate(item.getFechaUltimaActualizacionPrecio()));
            row.createCell(7).setCellValue(item.getActivate() != null ? item.getActivate() : 0);
            row.createCell(8).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(9).setCellValue(item.getTotalVentas() != null ? item.getTotalVentas() : 0);
            row.createCell(10).setCellValue(item.getFechaUltimaVenta() != null ? item.getFechaUltimaVenta().toString() : "");
            row.createCell(11).setCellValue(item.getPorcentajeGanancia() != null ? item.getPorcentajeGanancia() : 0);
        }
    }

    private void crearHojaHistorialProductos(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Historial_productos");
        String[] headers = {"ID", "Producto ID", "Evento", "Precio", "Fecha Creación", "Activo"};
        createHeaderRow(sheet, headers);

        List<HistorialProducto> list = historialProductoService.findAll();
        int rowIdx = 1;
        for (HistorialProducto item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getProductoId() != null ? item.getProductoId() : 0);
            row.createCell(2).setCellValue(clean(item.getEvento()));
            row.createCell(3).setCellValue(item.getPrecio() != null ? item.getPrecio().doubleValue() : 0.0);
            row.createCell(4).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(5).setCellValue(item.getActivo() != null && item.getActivo() ? "SI" : "NO");
        }
    }

    private void crearHojaHistorialRecibo(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Historial recibo");
        String[] headers = {"ID", "Cliente ID", "Fecha Creación", "Estado ID", "Método Pago ID", "Sesión ID", "Total", "Monto Recibido"};
        createHeaderRow(sheet, headers);

        List<HistorialRecibo> list = historialReciboService.findAll();
        int rowIdx = 1;
        for (HistorialRecibo item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getClienteId() != null ? item.getClienteId() : 0);
            row.createCell(2).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(3).setCellValue(item.getEstadoId() != null ? item.getEstadoId() : 0);
            row.createCell(4).setCellValue(item.getMetodoPagoId() != null ? item.getMetodoPagoId() : 0);
            row.createCell(5).setCellValue(item.getSesionId() != null ? item.getSesionId() : 0);
            row.createCell(6).setCellValue(item.getTotal() != null ? item.getTotal().doubleValue() : 0.0);
            row.createCell(7).setCellValue(item.getMontoRecibido() != null ? item.getMontoRecibido().doubleValue() : 0.0);
        }
    }

    private void crearHojaHistorialReciboDetalle(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Historial recibo detalle");
        String[] headers = {"ID", "Recibo ID", "Producto ID", "Cantidad", "Subtotal", "Usuario Creación", "Fecha Creación"};
        createHeaderRow(sheet, headers);

        List<HistorialReciboDetalle> list = historialReciboDetalleRepository.findAll();
        int rowIdx = 1;
        for (HistorialReciboDetalle item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getReciboId() != null ? item.getReciboId() : 0);
            row.createCell(2).setCellValue(item.getProductoId() != null ? item.getProductoId() : 0);
            row.createCell(3).setCellValue(item.getCantidad() != null ? item.getCantidad() : 0);
            row.createCell(4).setCellValue(item.getSubtotal() != null ? item.getSubtotal().doubleValue() : 0.0);
            row.createCell(5).setCellValue(item.getUsuarioCreacion() != null ? item.getUsuarioCreacion().toString() : "");
            row.createCell(6).setCellValue(formatDate(item.getFechaCreacion()));
        }
    }

    private void crearHojaCorteVenta(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Corte de venta");
        String[] headers = {"ID", "Usuario ID", "Fecha Creación", "Fecha Inicio", "Fecha Fin", "Último Historial Recibo ID", "Total", "Total Sistema"};
        createHeaderRow(sheet, headers);

        List<CorteVenta> list = corteVentaService.findAll();
        int rowIdx = 1;
        for (CorteVenta item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getUsuarioId()));
            row.createCell(2).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(3).setCellValue(formatDate(item.getFechaIni()));
            row.createCell(4).setCellValue(formatDate(item.getFechaFin()));
            row.createCell(5).setCellValue(item.getUltimoHistorialReciboId() != null ? item.getUltimoHistorialReciboId() : 0);
            row.createCell(6).setCellValue(item.getTotal() != null ? item.getTotal().doubleValue() : 0.0);
            row.createCell(7).setCellValue(item.getTotalSistema() != null ? item.getTotalSistema().doubleValue() : 0.0);
        }
    }

    private void crearHojaVentasTipo(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Ventas tipo");
        String[] headers = {"ID", "Método Pago ID", "Total", "Total Sistema", "Corte Venta ID"};
        createHeaderRow(sheet, headers);

        List<VentasTipo> list = ventasTipoService.findAll();
        int rowIdx = 1;
        for (VentasTipo item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getMetodoPagoId() != null ? item.getMetodoPagoId() : 0);
            row.createCell(2).setCellValue(item.getTotal() != null ? item.getTotal().doubleValue() : 0.0);
            row.createCell(3).setCellValue(item.getTotalSistema() != null ? item.getTotalSistema().doubleValue() : 0.0);
            row.createCell(4).setCellValue(item.getCorteVentaId() != null ? item.getCorteVentaId() : 0);
        }
    }

    private void crearHojaEvento(Workbook workbook) {
        Sheet sheet = workbook.createSheet("evento");
        String[] headers = {"ID", "Nombre", "Sigla"};
        createHeaderRow(sheet, headers);

        List<Evento> list = eventoService.findAll();
        int rowIdx = 1;
        for (Evento item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getNombre()));
            row.createCell(2).setCellValue(clean(item.getSigla()));
        }
    }

    private void crearHojaBitacoraUsuario(Workbook workbook) {
        Sheet sheet = workbook.createSheet("bitacora_usuario");
        String[] headers = {"ID", "User ID", "Evento ID", "Valor Antes", "Valor Después", "Referencia ID", "Fecha Creación"};
        createHeaderRow(sheet, headers);

        List<BitacoraUsuario> list = bitacoraUsuarioService.findAll();
        int rowIdx = 1;
        for (BitacoraUsuario item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getUserId() != null ? item.getUserId().toString() : "");
            row.createCell(2).setCellValue(item.getEventoId() != null ? item.getEventoId() : 0);
            row.createCell(3).setCellValue(clean(item.getValorAntes()));
            row.createCell(4).setCellValue(clean(item.getValorDespues()));
            row.createCell(5).setCellValue(item.getReferenciaId() != null ? item.getReferenciaId() : 0);
            row.createCell(6).setCellValue(formatDate(item.getFechaCreacion()));
        }
    }

    private void crearHojaUsuarios(Workbook workbook, String token) {
        Sheet sheet = workbook.createSheet("Usuarios");
        String[] headers = {"ID", "Nombre", "Correo Electrónico", "Teléfono", "Roles"};
        createHeaderRow(sheet, headers);

        List<AuthUserDto> list = authClient.getUsuarios(token);
        int rowIdx = 1;
        for (AuthUserDto item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId().toString() : "");
            row.createCell(1).setCellValue(clean(item.getNombre()));
            row.createCell(2).setCellValue(clean(item.getCorreoElectronico()));
            row.createCell(3).setCellValue(clean(item.getTelefono()));
            String roles = item.getRoles() != null
                    ? item.getRoles().stream().map(r -> r.getNombre()).collect(Collectors.joining(", "))
                    : "";
            row.createCell(4).setCellValue(clean(roles));
        }
    }

    private void crearHojaConfiguracionApp(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Configuracion App");
        String[] headers = {"ID", "Key", "Value"};
        createHeaderRow(sheet, headers);

        List<ConfiguracionApp> list = configuracionAppRepository.findAll();
        int rowIdx = 1;
        for (ConfiguracionApp item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getKey()));
            row.createCell(2).setCellValue(clean(item.getValue()));
        }
    }

    private void crearHojaEstadoRecibos(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Estado Recibos");
        String[] headers = {"ID", "Descripción", "Sigla"};
        createHeaderRow(sheet, headers);

        List<EstadoRecibo> list = estadoReciboRepository.findAll();
        int rowIdx = 1;
        for (EstadoRecibo item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getDescripcion()));
            row.createCell(2).setCellValue(clean(item.getSigla()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HOJAS NUEVAS
    // ─────────────────────────────────────────────────────────────────────────

    private void crearHojaClients(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Clientes");
        String[] headers = {"ID", "Nombre", "Teléfono", "Documento"};
        createHeaderRow(sheet, headers);

        List<Client> list = clientRepository.findAll();
        int rowIdx = 1;
        for (Client item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getNombre()));
            row.createCell(2).setCellValue(clean(item.getTelefono()));
            row.createCell(3).setCellValue(clean(item.getDocumento()));
        }
    }

    private void crearHojaCompany(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Company");
        String[] headers = {"ID", "Nombre", "Descripción", "Email", "Teléfono", "Nombre Contacto"};
        createHeaderRow(sheet, headers);

        List<Company> list = companyRepository.findAll();
        int rowIdx = 1;
        for (Company item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getName()));
            row.createCell(2).setCellValue(clean(item.getDescription()));
            row.createCell(3).setCellValue(clean(item.getEmail()));
            row.createCell(4).setCellValue(clean(item.getTelefono()));
            row.createCell(5).setCellValue(clean(item.getContactName()));
        }
    }

    private void crearHojaMetodoPago(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Metodo Pago");
        String[] headers = {"ID", "Descripción", "Estado", "File", "Sigla", "Color"};
        createHeaderRow(sheet, headers);

        List<MetodoPago> list = metodoPagoRepository.findAll();
        int rowIdx = 1;
        for (MetodoPago item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getDescripcion()));
            row.createCell(2).setCellValue(clean(item.getEstado()));
            row.createCell(3).setCellValue(clean(item.getFile()));
            row.createCell(4).setCellValue(clean(item.getSigla()));
            row.createCell(5).setCellValue(clean(item.getColor()));
        }
    }

    private void crearHojaRecibo(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Recibo");
        String[] headers = {
                "ID", "Cliente ID", "Fecha Creación", "Estado ID",
                "Método Pago ID", "Sesión ID", "Total", "Monto Recibido", "Recibo Padre ID"
        };
        createHeaderRow(sheet, headers);

        List<Recibo> list = reciboRepository.findAll();
        int rowIdx = 1;
        for (Recibo item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getClienteId() != null ? item.getClienteId() : 0);
            row.createCell(2).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(3).setCellValue(item.getEstadoId() != null ? item.getEstadoId() : 0);
            row.createCell(4).setCellValue(item.getMetodoPagoId() != null ? item.getMetodoPagoId() : 0);
            row.createCell(5).setCellValue(item.getSesionId() != null ? item.getSesionId() : 0);
            row.createCell(6).setCellValue(item.getTotal() != null ? item.getTotal().doubleValue() : 0.0);
            row.createCell(7).setCellValue(item.getMontoRecibido() != null ? item.getMontoRecibido().doubleValue() : 0.0);
            row.createCell(8).setCellValue(item.getReciboPadreId() != null ? item.getReciboPadreId() : 0);
        }
    }

    private void crearHojaReciboDetalle(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Recibo Detalle");
        String[] headers = {"ID", "Recibo ID", "Producto ID", "Cantidad", "Subtotal", "Fecha Creación", "Usuario Creación"};
        createHeaderRow(sheet, headers);

        List<ReciboDetalle> list = reciboDetalleRepository.findAll();
        int rowIdx = 1;
        for (ReciboDetalle item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getReciboId() != null ? item.getReciboId() : 0);
            row.createCell(2).setCellValue(item.getProductoId() != null ? item.getProductoId() : 0);
            row.createCell(3).setCellValue(item.getCantidad() != null ? item.getCantidad() : 0);
            row.createCell(4).setCellValue(item.getSubtotal() != null ? item.getSubtotal().doubleValue() : 0.0);
            row.createCell(5).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(6).setCellValue(item.getUsuarioCreacion() != null ? item.getUsuarioCreacion().toString() : "");
        }
    }

    private void crearHojaReciboDetalleHistorico(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Recibo Detalle Historico");
        String[] headers = {"ID", "Recibo Detalle ID", "Fecha Hora", "Usuario ID", "Acción"};
        createHeaderRow(sheet, headers);

        List<ReciboDetalleHistorico> list = reciboDetalleHistoricoRepository.findAll();
        int rowIdx = 1;
        for (ReciboDetalleHistorico item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getReciboDetalleId() != null ? item.getReciboDetalleId() : 0);
            row.createCell(2).setCellValue(formatDate(item.getFechaHora()));
            row.createCell(3).setCellValue(clean(item.getUsuarioId()));
            row.createCell(4).setCellValue(clean(item.getAccion()));
        }
    }

    private void crearHojaEdicionRecibo(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Edicion Recibo");
        String[] headers = {
                "ID", "Recibo ID", "Historial Recibo ID", "Cliente ID", "Fecha Creación",
                "Estado ID", "Método Pago ID", "Sesión ID", "Total", "Monto Recibido"
        };
        createHeaderRow(sheet, headers);

        List<EdicionRecibo> list = edicionReciboRepository.findAll();
        int rowIdx = 1;
        for (EdicionRecibo item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getReciboId() != null ? item.getReciboId() : 0);
            row.createCell(2).setCellValue(item.getHistorialReciboId() != null ? item.getHistorialReciboId() : 0);
            row.createCell(3).setCellValue(item.getClienteId() != null ? item.getClienteId() : 0);
            row.createCell(4).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(5).setCellValue(item.getEstadoId() != null ? item.getEstadoId() : 0);
            row.createCell(6).setCellValue(item.getMetodoPagoId() != null ? item.getMetodoPagoId() : 0);
            row.createCell(7).setCellValue(item.getSesionId() != null ? item.getSesionId() : 0);
            row.createCell(8).setCellValue(item.getTotal() != null ? item.getTotal().doubleValue() : 0.0);
            row.createCell(9).setCellValue(item.getMontoRecibido() != null ? item.getMontoRecibido().doubleValue() : 0.0);
        }
    }

    private void crearHojaEdicionReciboDetalle(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Edicion Recibo Detalle");
        String[] headers = {"ID", "Edición ID", "Producto ID", "Cantidad", "Subtotal"};
        createHeaderRow(sheet, headers);

        List<EdicionReciboDetalle> list = edicionReciboDetalleRepository.findAll();
        int rowIdx = 1;
        for (EdicionReciboDetalle item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getEdicionId() != null ? item.getEdicionId() : 0);
            row.createCell(2).setCellValue(item.getProductoId() != null ? item.getProductoId() : 0);
            row.createCell(3).setCellValue(item.getCantidad() != null ? item.getCantidad() : 0);
            row.createCell(4).setCellValue(item.getSubtotal() != null ? item.getSubtotal().doubleValue() : 0.0);
        }
    }

    private void crearHojaEgreso(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Egreso");
        String[] headers = {"ID", "Fecha Creación", "Fecha", "Valor", "Descripción", "Proveedor ID"};
        createHeaderRow(sheet, headers);

        List<Egreso> list = egresoRepository.findAll();
        int rowIdx = 1;
        for (Egreso item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(2).setCellValue(item.getFecha() != null ? formatLocalDate(item.getFecha()) : "");
            row.createCell(3).setCellValue(item.getValor() != null ? item.getValor().doubleValue() : 0.0);
            row.createCell(4).setCellValue(clean(item.getDescripcion()));
            row.createCell(5).setCellValue(item.getProveedor() != null ? item.getProveedor().getId() : 0);
        }
    }

    private void crearHojaProveedor(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Proveedor");
        String[] headers = {"ID", "Documento", "Nombre", "Teléfono", "Correo", "Tipo Egreso ID"};
        createHeaderRow(sheet, headers);

        List<Proveedor> list = proveedorRepository.findAll();
        int rowIdx = 1;
        for (Proveedor item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getDocumento()));
            row.createCell(2).setCellValue(clean(item.getNombre()));
            row.createCell(3).setCellValue(clean(item.getTelefono()));
            row.createCell(4).setCellValue(clean(item.getCorreo()));
            row.createCell(5).setCellValue(item.getTipoEgreso() != null ? item.getTipoEgreso().getId() : 0);
        }
    }

    private void crearHojaTipoEgreso(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Tipo Egreso");
        String[] headers = {"ID", "Nombre", "Descripción"};
        createHeaderRow(sheet, headers);

        List<TipoEgreso> list = tipoEgresoRepository.findAll();
        int rowIdx = 1;
        for (TipoEgreso item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getNombre()));
            row.createCell(2).setCellValue(clean(item.getDescripcion()));
        }
    }

    private void crearHojaSesion(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Sesion");
        String[] headers = {"ID", "Cookie", "Último Ticket ID", "User ID", "Fecha Inicio", "Fecha Fin", "Es Activo"};
        createHeaderRow(sheet, headers);

        List<Sesion> list = sesionRepository.findAll();
        int rowIdx = 1;
        for (Sesion item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getCookie()));
            row.createCell(2).setCellValue(item.getUltimoTicketId() != null ? item.getUltimoTicketId() : 0);
            row.createCell(3).setCellValue(item.getUserId() != null ? item.getUserId().toString() : "");
            row.createCell(4).setCellValue(formatDate(item.getFechaInicio()));
            row.createCell(5).setCellValue(formatDate(item.getFechaFin()));
            row.createCell(6).setCellValue(item.getEsActivo() != null && item.getEsActivo() ? "SI" : "NO");
        }
    }

    private void crearHojaTicket(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Ticket");
        String[] headers = {"ID", "Sesión ID", "Nombre", "Fecha Creación", "Orden"};
        createHeaderRow(sheet, headers);

        List<Ticket> list = ticketRepository.findAll();
        int rowIdx = 1;
        for (Ticket item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getSessionId() != null ? item.getSessionId() : 0);
            row.createCell(2).setCellValue(clean(item.getNombre()));
            row.createCell(3).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(4).setCellValue(item.getOrden() != null ? item.getOrden() : 0);
        }
    }

    private void crearHojaTicketRecibo(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Ticket Recibo");
        String[] headers = {"ID", "Ticket ID", "Recibo ID"};
        createHeaderRow(sheet, headers);

        List<TicketRecibo> list = ticketReciboRepository.findAll();
        int rowIdx = 1;
        for (TicketRecibo item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getTicketId() != null ? item.getTicketId() : 0);
            row.createCell(2).setCellValue(item.getReciboId() != null ? item.getReciboId() : 0);
        }
    }

    private void crearHojaFlujoDinero(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Flujo Dinero");
        String[] headers = {"ID", "Fecha", "Tipo ID", "Total", "User ID"};
        createHeaderRow(sheet, headers);

        List<FlujoDinero> list = flujoDineroRepository.findAll();
        int rowIdx = 1;
        for (FlujoDinero item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getFecha() != null ? formatLocalDate(item.getFecha()) : "");
            row.createCell(2).setCellValue(item.getTipoId() != null ? item.getTipoId() : 0);
            row.createCell(3).setCellValue(item.getTotal() != null ? item.getTotal().doubleValue() : 0.0);
            row.createCell(4).setCellValue(item.getUserId() != null ? item.getUserId() : 0);
        }
    }

    private void crearHojaEstadisticaFin(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Estadistica Fin");
        String[] headers = {
                "ID", "Fecha Creación", "Total Egresos", "Total Ventas", "Utilidad",
                "Porcentaje Utilidad", "Formato Tiempo", "Valor Tiempo", "Tipo Resultado Fin ID"
        };
        createHeaderRow(sheet, headers);

        List<EstadisticaFin> list = estadisticaFinRepository.findAll();
        int rowIdx = 1;
        for (EstadisticaFin item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(2).setCellValue(item.getTotalEgresos() != null ? item.getTotalEgresos().doubleValue() : 0.0);
            row.createCell(3).setCellValue(item.getTotalVentas() != null ? item.getTotalVentas().doubleValue() : 0.0);
            row.createCell(4).setCellValue(item.getUtilidad() != null ? item.getUtilidad().doubleValue() : 0.0);
            row.createCell(5).setCellValue(item.getPorcentajeUtilidad() != null ? item.getPorcentajeUtilidad().doubleValue() : 0.0);
            row.createCell(6).setCellValue(clean(item.getFormatoTiempo()));
            row.createCell(7).setCellValue(clean(item.getValorTiempo()));
            row.createCell(8).setCellValue(item.getTipoResultadoFin() != null ? item.getTipoResultadoFin().getId() : 0);
        }
    }

    private void crearHojaTipoResultadoFin(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Tipo Resultado Fin");
        String[] headers = {"ID", "Sigla", "Descripción", "Color"};
        createHeaderRow(sheet, headers);

        List<TipoResultadoFin> list = tipoResultadoFinRepository.findAll();
        int rowIdx = 1;
        for (TipoResultadoFin item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getSigla()));
            row.createCell(2).setCellValue(clean(item.getDescripcion()));
            row.createCell(3).setCellValue(clean(item.getColor()));
        }
    }

    private void crearHojaCargueProductos(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Cargue Productos");
        String[] headers = {
                "ID", "Nombre", "Fecha Creación", "Total Migrados",
                "Total Conflictos", "Total Conflictos Resueltos", "Mensajes Error"
        };
        createHeaderRow(sheet, headers);

        List<CargueProducto> list = cargueProductoRepository.findAll();
        int rowIdx = 1;
        for (CargueProducto item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(clean(item.getNombre()));
            row.createCell(2).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(3).setCellValue(item.getTotalMigrados() != null ? item.getTotalMigrados() : 0);
            row.createCell(4).setCellValue(item.getTotalConflictos() != null ? item.getTotalConflictos() : 0);
            row.createCell(5).setCellValue(item.getTotalConflictosResultos() != null ? item.getTotalConflictosResultos() : 0);
            row.createCell(6).setCellValue(clean(item.getMensajesError()));
        }
    }

    private void crearHojaCargueProductoConflictos(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Cargue Prod Conflictos");
        String[] headers = {"ID", "Cargue Prod ID", "Tipo Conflicto ID", "Nombre Producto", "Datos Conflicto", "Resuelto"};
        createHeaderRow(sheet, headers);

        List<CargueProductoConflicto> list = cargueProductoConflictoRepository.findAll();
        int rowIdx = 1;
        for (CargueProductoConflicto item : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getId() != null ? item.getId() : 0);
            row.createCell(1).setCellValue(item.getCargueProducto() != null ? item.getCargueProducto().getId() : 0);
            row.createCell(2).setCellValue(item.getTipoConflictoId() != null ? item.getTipoConflictoId() : 0);
            row.createCell(3).setCellValue(clean(item.getNombreProducto()));
            row.createCell(4).setCellValue(clean(item.getDatosConflicto()));
            row.createCell(5).setCellValue(item.getResuelto() != null && item.getResuelto() ? "SI" : "NO");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────────────────

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle style = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        style.setFont(font);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String formatLocalDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String clean(String text) {
        return StringUtils.cleanForExcel(text);
    }
}
