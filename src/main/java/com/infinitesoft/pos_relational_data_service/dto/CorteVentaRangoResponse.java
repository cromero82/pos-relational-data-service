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
        /** Ventas POS netas del periodo (solo pagadas). */
        private BigDecimal totalVentasSistema;
        /** Egresos registrados con este medio de pago. */
        private BigDecimal totalEgresosSistema;
        /**
         * Otros movimientos del ledger (traslados, entradas manuales, ajustes).
         * No incluye SALIDA_EGRESO de egreso documento ni ENTRADA_VENTA (columnas propias).
         * Sí incluye ajustes {@code QR_MONTO_DISTINTO} aunque el tipo sea SALIDA_EGRESO.
         */
        private BigDecimal totalMovimientosSistema;
        /**
         * Base provisional del turno: total físico declarado en el corte anterior
         * (hasta existir Distribución de efectivo / BASE_TURNO).
         */
        private BigDecimal base;
        /** Neto sistema = base + ventas − egresos + movimientos. */
        private BigDecimal totalSistema;
    }
}
