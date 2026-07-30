package com.infinitesoft.pos_relational_data_service.entities.enums;

public enum TipoMovimientoOrigenFondos {
    ENTRADA_MANUAL,
    ENTRADA_PRESTAMO,
    /** Ventas del periodo contabilizadas al confirmar el corte (hasta existir posteo por ticket). */
    ENTRADA_VENTA,
    TRASLADO,
    SALIDA_EGRESO,
    SALIDA_DEVOLUCION_PRESTAMO,
    AJUSTE_SALDO,
    AJUSTE_CIERRE,
    REVERSO_AJUSTE_CIERRE,
    REVERSO_ENTRADA_VENTA
}
