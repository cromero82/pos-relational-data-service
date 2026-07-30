package com.infinitesoft.pos_relational_data_service.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncionalidadRolId implements Serializable {
    private Integer funcionalidadId;
    private String rolSigla;
}
