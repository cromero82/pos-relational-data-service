package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SPLIT / editor de un corte de ventas mal generado.
 * particiones.size() == 1 (mismo rango que el corte original) => modo editor: no reestructura
 * el ledger, solo deja traza (corte_venta_correccion tipo EDICION).
 * particiones.size() >= 2 => SPLIT completo: reverso + puente + re-corte por partición.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividirCorteRequest {
    /** Obligatorio en toda corrección (SPLIT o EDICION). */
    private String motivo;
    private List<Particion> particiones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Particion {
        private LocalDateTime fechaDesde;
        private LocalDateTime fechaHasta;
        /** Prorrateo editable a Caja Menor para esta partición (solo relevante si N&gt;=2). */
        private BigDecimal montoCajaMenor;
        /** Prorrateo editable a Caja General para esta partición (solo relevante si N&gt;=2). */
        private BigDecimal montoCajaGeneral;
    }
}
