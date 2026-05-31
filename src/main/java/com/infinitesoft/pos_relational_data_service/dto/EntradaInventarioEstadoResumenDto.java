package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.enums.EntradaInventarioEstado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaInventarioEstadoResumenDto {
    private Long egresoId;
    private Long entradaId;
    private EntradaInventarioEstado estado;
    private Integer totalItems;
}
