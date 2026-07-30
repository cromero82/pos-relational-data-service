package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseInicialPendienteDto {
    private boolean pendiente;
    private Integer cajaEfectivoId;
    private String cajaEfectivoNombre;
    private Integer motivoMovimientoId;
    private String motivoMovimientoNombre;
}
