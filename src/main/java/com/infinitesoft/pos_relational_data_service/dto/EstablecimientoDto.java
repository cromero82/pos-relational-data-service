package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstablecimientoDto {
    private Long id;
    private String razonSocial;
    private String nombreComercial;
    private String nit;
    private String digitoVerificacion;
    private String regimenTributario;
    private String regimenLeyendaImpresion;
    private String direccion;
    private String telefono;
    private String email;
    private Boolean manejoEstrictoCuentas;
}
