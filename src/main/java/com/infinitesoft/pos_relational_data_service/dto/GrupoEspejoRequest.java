package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoEspejoRequest {

    @NotBlank
    private String nombre;

    private List<Long> productoIds;
}
