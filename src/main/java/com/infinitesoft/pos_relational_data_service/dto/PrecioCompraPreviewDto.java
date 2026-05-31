package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrecioCompraPreviewDto {
    private Long productoId;
    private String productoNombre;
    private String productoBarcode;
    private BigDecimal precioCompraAnterior;
    private BigDecimal precioCompraNuevo;
    private BigDecimal precioVentaActual;
    private Short porcentajeGanancia;
    private String porcentajeGananciaDisplay;
    private BigDecimal porcentajeVariacionCompra;
    private String porcentajeVariacionCompraDisplay;
    private boolean precioCompraCambio;
    private boolean alertaPrecioSubio;
    private boolean tienePrecioCompraAnterior;
}
