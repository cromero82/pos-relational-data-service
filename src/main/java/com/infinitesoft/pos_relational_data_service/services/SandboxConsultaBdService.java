package com.infinitesoft.pos_relational_data_service.services;

import java.util.Map;

public interface SandboxConsultaBdService {

    /**
     * Ejecuta SQL de solo lectura si
     * {@code configuracion_app.sistema.sandbox.habilitarEndpointConsultaBd=true}.
     */
    Map<String, Object> consultar(String sql, Integer maxRows);
}
