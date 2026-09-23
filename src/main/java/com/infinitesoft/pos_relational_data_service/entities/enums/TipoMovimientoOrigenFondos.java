package com.infinitesoft.pos_relational_data_service.entities.enums;

public enum TipoMovimientoOrigenFondos {
    ENTRADA_MANUAL,
    ENTRADA_PRESTAMO,
    /** Ventas del periodo contabilizadas al confirmar el corte (hasta existir posteo por ticket). */
    ENTRADA_VENTA,
    /**
     * Cobranza de cuenta por cobrar (abono). Entra a caja/OF del medio;
     * no suma a Ventas del día ({@code ENTRADA_VENTA}).
     * En el dashboard de Ingresos va en la columna Cobranzas (no se mezcla con Ventas).
     * Sí suma en Cierre de turno (columna Movimientos).
     */
    ENTRADA_COBRANZA,
    TRASLADO,
    SALIDA_EGRESO,
    SALIDA_DEVOLUCION_PRESTAMO,
    AJUSTE_SALDO,
    AJUSTE_CIERRE,
    REVERSO_AJUSTE_CIERRE,
    REVERSO_ENTRADA_VENTA
}
