package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuncionalidadPermisoDto {
    private String codigo;
    private String nombre;
    private String seccion;
    private String rutaFront;
    private String etiquetaMenu;
    private String badgeUi;
    private boolean requiereDisclaimer;
    private boolean puedeEscribir;
}
