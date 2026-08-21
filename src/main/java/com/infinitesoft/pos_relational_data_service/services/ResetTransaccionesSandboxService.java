package com.infinitesoft.pos_relational_data_service.services;

import java.util.Map;

public interface ResetTransaccionesSandboxService {

    /**
     * Vacía tablas transaccionales (ventas, cortes, ledger, egresos, CxC, notifs…)
     * y conserva catálogos / paramétricas. Solo si {@code configuracion_app.sistema}
     * habilita el endpoint sandbox.
     */
    Map<String, Object> resetTransacciones();
}
