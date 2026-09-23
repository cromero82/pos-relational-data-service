package com.infinitesoft.pos_relational_data_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteVentaRangoRequest {
    private LocalDateTime fechaIni;
    private LocalDateTime fechaFin;
    private boolean ultimoCorte;
    private boolean actual;

    // Las validaciones de "Si el campo 'ultimocorte' es falso ¿Entonces fechaini No puede ser nulo" 
    // y "actual es falso Entonces el campo 'fechafin' no puede ser nulo"
    // se realizarán en el servicio o mediante una validación personalizada.
}
