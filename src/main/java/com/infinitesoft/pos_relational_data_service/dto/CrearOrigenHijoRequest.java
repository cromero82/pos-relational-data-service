package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearOrigenHijoRequest {
    /** Padre bajo el cual se crea el fondo hijo. */
    private Integer parentOrigenFondosId;
    /** Nombre del bolsillo (único entre hermanos activos). */
    private String nombre;
    /** Opcional; si null se hereda el tipo del padre o se usa OTRO. */
    private Integer tipoOrigenFondosId;
}
