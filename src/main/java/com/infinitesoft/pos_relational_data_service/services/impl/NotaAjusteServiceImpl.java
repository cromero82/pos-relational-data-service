package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.DocumentoVentaDto;
import com.infinitesoft.pos_relational_data_service.dto.HistorialDocumentosDto;
import com.infinitesoft.pos_relational_data_service.dto.NotaAjusteResumenDto;
import com.infinitesoft.pos_relational_data_service.entities.DocumentoVenta;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.MotivoOperacion;
import com.infinitesoft.pos_relational_data_service.entities.NotaAjusteDocumento;
import com.infinitesoft.pos_relational_data_service.entities.enums.DocumentoVentaEstado;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoConsecutivoDocumento;
import com.infinitesoft.pos_relational_data_service.repositories.DocumentoVentaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.MotivoOperacionRepository;
import com.infinitesoft.pos_relational_data_service.repositories.NotaAjusteDocumentoRepository;
import com.infinitesoft.pos_relational_data_service.services.ConsecutivoDocumentoService;
import com.infinitesoft.pos_relational_data_service.services.DocumentoVentaService;
import com.infinitesoft.pos_relational_data_service.services.NotaAjusteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotaAjusteServiceImpl implements NotaAjusteService {

    @Autowired
    private NotaAjusteDocumentoRepository notaAjusteRepository;

    @Autowired
    private MotivoOperacionRepository motivoOperacionRepository;

    @Autowired
    private DocumentoVentaRepository documentoVentaRepository;

    @Autowired
    private HistorialReciboRepository historialReciboRepository;

    @Autowired
    private ConsecutivoDocumentoService consecutivoDocumentoService;

    @Autowired
    private DocumentoVentaService documentoVentaService;

    @Override
    @Transactional
    public NotaAjusteDocumento crearNotaCreditoAnulacion(
            DocumentoVenta documentoVenta,
            HistorialRecibo historialRecibo,
            String motivoCodigo,
            String motivoTexto,
            boolean operacionRestauracion,
            UUID usuarioId) {

        if (documentoVenta == null || documentoVenta.getId() == null) {
            throw new IllegalArgumentException("Se requiere documento de venta para la nota crédito");
        }
        if (documentoVenta.getEstado() == DocumentoVentaEstado.ANULADO) {
            throw new IllegalStateException("El documento de venta ya está anulado");
        }

        String codigo = motivoCodigo != null && !motivoCodigo.isBlank()
                ? motivoCodigo
                : (operacionRestauracion ? "RESTAURACION_TICKET" : "ANULACION_ADMIN");

        MotivoOperacion motivo = motivoOperacionRepository.findByCodigoAndActivoTrue(codigo)
                .orElse(null);

        String consecutivo = consecutivoDocumentoService.nextConsecutivo(TipoConsecutivoDocumento.NC);
        int anio = documentoVenta.getAnio() != null
                ? documentoVenta.getAnio()
                : java.time.Year.now().getValue();

        NotaAjusteDocumento nota = NotaAjusteDocumento.builder()
                .tipo("CREDITO")
                .consecutivo(consecutivo)
                .anio(anio)
                .documentoVentaOrigenId(documentoVenta.getId())
                .motivoOperacionId(motivo != null ? motivo.getId() : null)
                .motivoTexto(motivoTexto)
                .totalAjuste(documentoVenta.getTotal())
                .usuarioId(usuarioId)
                .historialReciboId(historialRecibo != null ? historialRecibo.getId() : null)
                .operacionRestauracion(operacionRestauracion)
                .build();

        NotaAjusteDocumento saved = notaAjusteRepository.save(nota);

        documentoVenta.setEstado(DocumentoVentaEstado.ANULADO);
        documentoVenta.setNotaAjusteAnulacionId(saved.getId());
        documentoVentaRepository.save(documentoVenta);

        return saved;
    }

    @Override
    public HistorialDocumentosDto getDocumentosByHistorialReciboId(Long historialReciboId) {
        HistorialDocumentosDto dto = HistorialDocumentosDto.builder().build();
        if (historialReciboId == null) {
            return dto;
        }

        Optional<DocumentoVenta> docOpt = documentoVentaRepository.findByHistorialReciboId(historialReciboId);
        if (docOpt.isEmpty()) {
            HistorialRecibo hr = historialReciboRepository.findById(historialReciboId).orElse(null);
            if (hr != null && hr.getDocumentoVentaId() != null) {
                docOpt = documentoVentaRepository.findById(hr.getDocumentoVentaId());
            }
        }

        docOpt.ifPresent(doc -> {
            DocumentoVentaDto docDto = documentoVentaService.toDto(doc);
            dto.setDocumentoVenta(docDto);
            List<NotaAjusteResumenDto> notas = notaAjusteRepository
                    .findByDocumentoVentaOrigenIdOrderByFechaHechoDesc(doc.getId())
                    .stream()
                    .map(this::toResumenDto)
                    .collect(Collectors.toList());
            dto.setNotasAjuste(notas);
            dto.setAnuladoConNc(!notas.isEmpty() && doc.getEstado() == DocumentoVentaEstado.ANULADO);
            dto.setRestaurado(notas.stream().anyMatch(n -> Boolean.TRUE.equals(n.getOperacionRestauracion())));
        });

        return dto;
    }

    @Override
    public NotaAjusteResumenDto toResumenDto(NotaAjusteDocumento entity) {
        if (entity == null) {
            return null;
        }
        String motivoCodigo = null;
        String motivoNombre = null;
        if (entity.getMotivoOperacionId() != null) {
            Optional<MotivoOperacion> mo = motivoOperacionRepository.findById(entity.getMotivoOperacionId());
            if (mo.isPresent()) {
                motivoCodigo = mo.get().getCodigo();
                motivoNombre = mo.get().getNombre();
            }
        }
        return NotaAjusteResumenDto.builder()
                .id(entity.getId())
                .tipo(entity.getTipo())
                .consecutivo(entity.getConsecutivo())
                .totalAjuste(entity.getTotalAjuste())
                .motivoCodigo(motivoCodigo)
                .motivoNombre(motivoNombre)
                .motivoTexto(entity.getMotivoTexto())
                .fechaHecho(entity.getFechaHecho())
                .operacionRestauracion(entity.getOperacionRestauracion())
                .build();
    }
}
