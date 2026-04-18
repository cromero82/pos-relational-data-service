package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoEspejoDetalleResponse {
    private Long id;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long productoReferenciaId;
    private List<ProductoEspejoDto> productos;
}
