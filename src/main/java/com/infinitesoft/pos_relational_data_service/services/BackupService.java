package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.*;
import com.infinitesoft.pos_relational_data_service.repositories.ConfiguracionAppRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EstadoReciboRepository;
import com.infinitesoft.pos_relational_data_service.security.client.AuthClient;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final ProductService productService;
    private final HistorialProductoService historialProductoService;
    private final HistorialReciboService historialReciboService;
    private final CorteVentaService corteVentaService;
    private final VentasTipoService ventasTipoService;
    private final EventoService eventoService;
    private final BitacoraUsuarioService bitacoraUsuarioService;
    private final AuthClient authClient;
    private final EmailService emailService;
    private final com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboDetalleRepository historialReciboDetalleRepository;
    private final ConfiguracionAppRepository configuracionAppRepository;
    private final EstadoReciboRepository estadoReciboRepository;

    public byte[] generarBackupExcel(String token) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
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

    private void crearHojaProductos(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Productos");
        String[] headers = {"ID", "Código Barras", "Nombre", "Precio", "Precio Compra", "Fecha Actualización Precio", "Activo", "Fecha Creación", "Total Ventas", "Fecha Última Venta", "Porcentaje Ganancia"};
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
            row.createCell(5).setCellValue(formatDate(item.getFechaUltimaActualizacionPrecio()));
            row.createCell(6).setCellValue(item.getActivate() != null ? item.getActivate() : 0);
            row.createCell(7).setCellValue(formatDate(item.getFechaCreacion()));
            row.createCell(8).setCellValue(item.getTotalVentas() != null ? item.getTotalVentas() : 0);
            row.createCell(9).setCellValue(item.getFechaUltimaVenta() != null ? item.getFechaUltimaVenta().toString() : "");
            row.createCell(10).setCellValue(item.getPorcentajeGanancia() != null ? item.getPorcentajeGanancia() : 0);
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
            String roles = item.getRoles() != null ? item.getRoles().stream().map(r -> r.getNombre()).collect(Collectors.joining(", ")) : "";
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

    private String clean(String text) {
        return StringUtils.cleanForExcel(text);
    }
}