package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialDocumentosDto {

    private DocumentoVentaDto documentoVenta;
    @Builder.Default
    private List<NotaAjusteResumenDto> notasAjuste = new ArrayList<>();
    private DocumentoVentaDto documentoVentaNuevo;
    private Boolean restaurado;
    private Boolean anuladoConNc;
}
