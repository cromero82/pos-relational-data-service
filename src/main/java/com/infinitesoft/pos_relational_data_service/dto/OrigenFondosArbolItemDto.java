package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrigenFondosArbolItemDto {
    private Integer id;
    private String nombre;
    /** Etiqueta con sangría visual para selects (──── hijo). */
    private String nombreDisplay;
    private Integer nivel;
    private Integer parentOrigenFondosId;
    private Long metodoPagoId;
    private String tipoOrigenFondosNombre;
    private String tipoOrigenFondosCodigo;
    private Boolean esRaiz;
    private Boolean visibleEnEgreso;
    private String color;
    private Integer orden;
    private BigDecimal saldo;
}
