package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.DocumentoVentaDto;
import com.infinitesoft.pos_relational_data_service.entities.DocumentoVenta;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;

import java.util.Optional;
import java.util.UUID;

public interface DocumentoVentaService {

    DocumentoVenta crearDesdeHistorialRecibo(HistorialRecibo historialRecibo, UUID usuarioId);

    Optional<DocumentoVentaDto> findDtoByHistorialReciboId(Long historialReciboId);

    DocumentoVentaDto toDto(DocumentoVenta entity);
}
