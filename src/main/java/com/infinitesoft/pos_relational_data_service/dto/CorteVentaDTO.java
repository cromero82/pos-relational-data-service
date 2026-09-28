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
    /** Contado / declarado (arqueo). */
    private BigDecimal total;
    /** Esperado = base + ventas − egresos ± movimientos. */
    private BigDecimal totalSistema;
    /** Σ tickets cobrados. Fuente de verdad para dashboard Ingresos. */
    private BigDecimal totalVentasSistema;
    /** Snapshot de indicadores al registrar. Nulos en cortes anteriores a la migración 74. */
    private BigDecimal kpiVentasTurno;
    private BigDecimal kpiEfectivoDisponible;
    private BigDecimal kpiMediosElectronicos;
    private BigDecimal kpiTotalDisponible;
    private BigDecimal kpiCartera;
    private BigDecimal kpiCarteraCobrada;
    /** this − corte de referencia (ayer / turno similar). */
    private BigDecimal kpiVentasTurnoDelta;
    private LocalDateTime kpiVentasTurnoReferenciaFecha;
    /** SUBE | BAJA | IGUAL */
    private String kpiVentasTurnoDireccion;
    /** Variación % de cartera vs corte anterior. */
    private BigDecimal kpiCarteraDeltaPct;
    /** SUBE | BAJA | IGUAL — SUBE es peor (más deuda). */
    private String kpiCarteraDireccion;
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
