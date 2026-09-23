package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrigenFondosDto {
    private Integer id;
    private String nombre;
    private Integer tipoOrigenFondosId;
    private String tipoOrigenFondosNombre;
    private String tipoOrigenFondosCodigo;
    private Long proveedorId;
    private Long metodoPagoId;
    private Integer parentOrigenFondosId;
    private String naturaleza;
    private Boolean visibleEnEgreso;
    private Boolean requiereConciliacion;
    private Boolean activo;
    private String estado;
    private Integer orden;
    private String color;
    private String notas;
    private BigDecimal saldo;
}
