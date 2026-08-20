package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;

@Data
public class CerrarCuentaPorCobrarRequest {
    /** Texto libre (obligatorio en castigo; recomendado en anulación). */
    private String motivoTexto;
    /** Opcional: override del motivo de catálogo. */
    private Long motivoOperacionId;
}
