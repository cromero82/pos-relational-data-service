package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarRevisionCorteRequest {
    private List<DetalleRevision> detalles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleRevision {
        private Long detalleId;
        private BigDecimal total;
        private Integer motivoDesfaseId;
        private String revisionEstado;
        private String revisionComentario;
    }
}
