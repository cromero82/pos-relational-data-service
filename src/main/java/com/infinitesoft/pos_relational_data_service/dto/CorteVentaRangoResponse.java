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
public class CorteVentaRangoResponse {
    private LocalDateTime fechaIni;
    private LocalDateTime fechaFin;
    private LocalDateTime ultimoCorte;
    private List<VentasTipoResumenDTO> ventasTipo;
    private BigDecimal total;
    private List<CorteVentaDTO> otrosCortesIntersectados;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VentasTipoResumenDTO {
        private Long metodoPagoId;
        private BigDecimal totalSistema;
    }
}
