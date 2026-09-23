package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.HistorialDocumentosDto;
import com.infinitesoft.pos_relational_data_service.dto.NotaAjusteResumenDto;
import com.infinitesoft.pos_relational_data_service.entities.DocumentoVenta;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.NotaAjusteDocumento;

import java.util.UUID;

public interface NotaAjusteService {

    NotaAjusteDocumento crearNotaCreditoAnulacion(
            DocumentoVenta documentoVenta,
            HistorialRecibo historialRecibo,
            String motivoCodigo,
            String motivoTexto,
            boolean operacionRestauracion,
            UUID usuarioId);

    HistorialDocumentosDto getDocumentosByHistorialReciboId(Long historialReciboId);

    NotaAjusteResumenDto toResumenDto(NotaAjusteDocumento entity);
}
