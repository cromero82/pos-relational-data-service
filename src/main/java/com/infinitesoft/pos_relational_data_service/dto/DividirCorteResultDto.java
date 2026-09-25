package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividirCorteResultDto {
    private boolean ok;
    private Long correccionId;
    /** SPLIT | EDICION */
    private String tipo;
    private Long corteOriginalId;
    /** Vacío en modo editor (EDICION); N cortes nuevos en modo SPLIT. */
    private List<Long> cortesNuevosIds;
}
