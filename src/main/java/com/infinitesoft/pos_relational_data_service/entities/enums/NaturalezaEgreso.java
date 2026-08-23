package com.infinitesoft.pos_relational_data_service.entities.enums;

/**
 * Propósito del pago en el egreso (código = {@code naturaleza_tipo_egreso.codigo}).
 * No incluye RETIRO_DUENO: acumulación del dueño es OF «Cuenta del dueño».
 */
public enum NaturalezaEgreso {
    COMPRA_MERCANCIA,
    GASTO_OPERATIVO,
    PERSONAL,
    DIVIDENDOS,
    TRIBUTO,
    OTRO;

    public static NaturalezaEgreso fromNullable(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return NaturalezaEgreso.valueOf(raw.trim().toUpperCase());
    }
}
