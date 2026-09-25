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
    REVERSO_ENTRADA_VENTA,
    /**
     * Reverso de un traslado de distribución de efectivo (por eliminación del corte).
     * @deprecated legacy: no generar nuevos movimientos con este tipo, usar {@link #REVERSO_TRASLADO}.
     *             Se conserva para no romper la deserialización de filas ya existentes.
     */
    @Deprecated
    REVERSO_TRASLADO_DISTRIBUCION,
    /** Reverso de un traslado (distribución de efectivo), por eliminación de corte o por SPLIT. */
    REVERSO_TRASLADO,
    /**
     * Asiento puente transitorio usado por el SPLIT de corte de ventas para evitar saldo negativo
     * en el OF destino (Caja Menor/General) cuando ya hubo egresos posteriores a la distribución
     * original. No es ingreso: se excluye de Ventas/Movimientos igual que DISTRIBUCION.
     */
    AJUSTE_PUENTE_SPLIT
}
