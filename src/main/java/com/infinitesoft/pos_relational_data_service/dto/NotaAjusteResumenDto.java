package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaAjusteResumenDto {

    private Long id;
    private String tipo;
    private String consecutivo;
    private BigDecimal totalAjuste;
    private String motivoCodigo;
    private String motivoNombre;
    private String motivoTexto;
    private String usuarioNombre;
    private LocalDateTime fechaHecho;
    private Boolean operacionRestauracion;
}
