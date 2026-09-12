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
    /**
     * Esperado del turno = Σ {@code totalSistema} (base + ventas − egresos ± movimientos).
     * No es la venta del día; ver {@link #totalVentasSistema}.
     */
    private BigDecimal total;
    /** Σ tickets cobrados del rango. Fuente de verdad para Ingresos / dashboard. */
    private BigDecimal totalVentasSistema;
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
         * Otros movimientos del ledger (traslados, entradas manuales, ajustes, cobranzas).
         * No incluye SALIDA_EGRESO de egreso documento ni ENTRADA_VENTA (columnas propias).
         * Sí incluye ajustes {@code QR_MONTO_DISTINTO} aunque el tipo sea SALIDA_EGRESO.
         * No incluye el débito PAGASTE ({@code MOVIMIENTO BANCO POR IDENTIFICAR}) de un
         * medio electrónico (QR, Nequi, …) cuando ese par ya se formalizó como egreso.
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
