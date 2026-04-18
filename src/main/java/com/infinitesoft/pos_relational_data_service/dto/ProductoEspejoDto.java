package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoEspejoDto {
    private Long id;
    private String nombre;
    private Double precio;
    private Double precioCompra;
    private Double precioUnidad;
    private Short porcentajeGanancia;
    private LocalDateTime fechaUltimaActualizacionPrecio;
}
