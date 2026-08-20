package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotivoMovimientoDto {
    private Integer id;
    private String codigo;
    private String nombre;
    private String categoria;
    private Boolean sistema;
    private Boolean activo;
    private Integer orden;
    /** TRASLADO_OF | REGISTRAR_DOCUMENTO | AJUSTE_CIERRE | REVISAR */
    private String accionEsperada;
}
