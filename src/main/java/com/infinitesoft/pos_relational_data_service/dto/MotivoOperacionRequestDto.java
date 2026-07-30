package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotivoOperacionRequestDto {

    private String motivoOperacionCodigo;
    private String motivoTexto;
}
