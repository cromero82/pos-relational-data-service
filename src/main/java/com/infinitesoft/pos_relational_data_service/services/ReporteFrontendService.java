package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.ReporteFrontendRequest;

public interface ReporteFrontendService {

    /**
     * Acepta un reporte de error frontend y lo persiste asincronamente en
     * InfluxDB (measurement {@code frontend_error}).
     */
    void registrar(ReporteFrontendRequest reporte);
}
