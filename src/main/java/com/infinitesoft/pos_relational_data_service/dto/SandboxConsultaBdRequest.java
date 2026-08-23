package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;

@Data
public class SandboxConsultaBdRequest {
    /** Solo SELECT / WITH … SELECT. */
    private String sql;
    /** Tope de filas (default 100, máx 500). */
    private Integer maxRows;
}
