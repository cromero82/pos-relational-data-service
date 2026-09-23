package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private List<FilterRequest> filtros;
    private int page = 0;
    private int size = 10;
    private String campoOrdenamiento = "nombre";
    private String orden = "asc";
}
