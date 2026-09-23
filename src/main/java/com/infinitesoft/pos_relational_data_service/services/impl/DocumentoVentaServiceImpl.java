package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.DocumentoVentaDto;
import com.infinitesoft.pos_relational_data_service.entities.DocumentoVenta;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.enums.DocumentoVentaEstado;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoConsecutivoDocumento;
import com.infinitesoft.pos_relational_data_service.repositories.DocumentoVentaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.ConsecutivoDocumentoService;
import com.infinitesoft.pos_relational_data_service.services.DocumentoVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentoVentaServiceImpl implements DocumentoVentaService {

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private HistorialReciboRepository historialReciboRepository;

    @Autowired
    private ConsecutivoDocumentoService consecutivoDocumentoService;

    @Override
    @Transactional
    public DocumentoVenta crearDesdeHistorialRecibo(HistorialRecibo historialRecibo, UUID usuarioId) {
        if (historialRecibo == null || historialRecibo.getId() == null) {
            throw new IllegalArgumentException("HistorialRecibo inválido para documento de venta");
        }

        Optional<DocumentoVenta> existing = documentoVentaRepository.findByHistorialReciboId(historialRecibo.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        String consecutivo = consecutivoDocumentoService.nextConsecutivo(TipoConsecutivoDocumento.VENTA);
        int anio = historialRecibo.getFechaCreacion() != null
                ? historialRecibo.getFechaCreacion().getYear()
                : java.time.Year.now().getValue();

        DocumentoVenta documento = DocumentoVenta.builder()
                .consecutivo(consecutivo)
                .anio(anio)
                .historialReciboId(historialRecibo.getId())
                .fechaHecho(historialRecibo.getFechaCreacion())
                .total(historialRecibo.getTotal())
                .metodoPagoId(historialRecibo.getMetodoPagoId())
                .clienteId(historialRecibo.getClienteId())
                .usuarioId(usuarioId)
                .sesionId(historialRecibo.getSesionId())
                .estado(DocumentoVentaEstado.VIGENTE)
                .build();

        DocumentoVenta saved = documentoVentaRepository.save(documento);

        HistorialRecibo histRef = historialReciboRepository.findById(historialRecibo.getId()).orElse(null);
        if (histRef != null) {
            histRef.setDocumentoVentaId(saved.getId());
            historialReciboRepository.save(histRef);
        }

        return saved;
    }

    @Override
    public Optional<DocumentoVentaDto> findDtoByHistorialReciboId(Long historialReciboId) {
        return documentoVentaRepository.findByHistorialReciboId(historialReciboId).map(this::toDto);
    }

    @Override
    public DocumentoVentaDto toDto(DocumentoVenta entity) {
        if (entity == null) {
            return null;
        }
        return DocumentoVentaDto.builder()
                .id(entity.getId())
                .consecutivo(entity.getConsecutivo())
                .anio(entity.getAnio())
                .historialReciboId(entity.getHistorialReciboId())
                .fechaHecho(entity.getFechaHecho())
                .total(entity.getTotal())
                .metodoPagoId(entity.getMetodoPagoId())
                .clienteId(entity.getClienteId())
                .estado(entity.getEstado() != null ? entity.getEstado().name() : null)
                .build();
    }
}
