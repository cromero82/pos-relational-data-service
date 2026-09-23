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
public class CorteVentaDetalleDTO {
    private Long id;
    private Long corteVentaId;
    private Long metodoPagoId;
    private Integer origenFondosId;
    private BigDecimal base;
    private BigDecimal totalVentasSistema;
    private BigDecimal totalEgresosSistema;
    private BigDecimal totalMovimientosSistema;
    private BigDecimal totalSistema;
    private BigDecimal total;
    private BigDecimal desfase;
    private Integer motivoDesfaseId;
    private String modoCaptura;
    private String declaradoPor;
    private String revisionEstado;
    private String revisionComentario;
    private Boolean ajusteGenerado;
    private Integer orden;
}
