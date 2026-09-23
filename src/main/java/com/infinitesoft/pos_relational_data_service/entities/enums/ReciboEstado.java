package com.infinitesoft.pos_relational_data_service.entities.enums;

/**
 * Enumerates the possible estados for a Recibo.
 * IDs map to DB values:
 * 1 = PENDIENTE_PAGO
 * 2 = PAGADO
 * 3 = ANULADO
 */
public enum ReciboEstado {
    PENDIENTE_PAGO(1L, "pendiente pago"),
    PAGADO(2L, "pagado"),
    ANULADO(3L, "anulado"),
    EDICION(4L, "edicion");

    private final Long id;
    private final String label;

    ReciboEstado(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static ReciboEstado fromId(Long id) {
        if (id == null) return null;
        for (ReciboEstado e : values()) {
            if (e.id.equals(id)) return e;
        }
        return null;
    }
}