package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteVentaDTO {
    private Long id;
    private String usuarioId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaIni;
    private LocalDateTime fechaFin;
    private Long ultimoHistorialReciboId;
    private Long ultimoMovimientoOrigenFondosId;
    private String distribucionEfectivoEstado;
    private BigDecimal baseSiguienteEfectivo;
    private BigDecimal total;
    private BigDecimal totalSistema;
    private List<VentasTipoDTO> ventasTipo;
    private List<CorteVentaDetalleDTO> detalles;
    private String estado;
    private String observacion;
    private String revisadoPor;
    private LocalDateTime fechaRevision;
    private Boolean ultimoVigente;
    private boolean ultimoCorte;
    private boolean actual;
}
