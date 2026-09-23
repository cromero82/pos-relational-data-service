package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalleHistorico;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboDetalleHistoricoRepository;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleHistoricoService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReciboDetalleHistoricoServiceImpl implements ReciboDetalleHistoricoService {

    private static final Logger logger = LoggerFactory.getLogger(ReciboDetalleHistoricoServiceImpl.class);

    @Autowired
    private ReciboDetalleHistoricoRepository repository;

    @Override
    @Transactional
    public ReciboDetalleHistorico registrarAccion(Long reciboDetalleId, String usuarioId, String accion) {
        ReciboDetalleHistorico historico = ReciboDetalleHistorico.builder()
                .reciboDetalleId(reciboDetalleId)
                .fechaHora(DateUtils.obtenerFechaSistema())
                .usuarioId(usuarioId)
                .accion(accion)
                .build();
        ReciboDetalleHistorico saved = repository.save(historico);

        if ("elimina".equals(accion)) {
            long agregaCount = repository.countByReciboDetalleIdAndAccion(reciboDetalleId, "agrega");
            long eliminaCount = repository.countByReciboDetalleIdAndAccion(reciboDetalleId, "elimina");

            if (agregaCount == eliminaCount) {
                repository.deleteByReciboDetalleId(reciboDetalleId);
                logger.info("El producto con recibo_detalle_id: {} ha sido eliminado completamente. Se han eliminado sus registros históricos.", reciboDetalleId);
            }
        }

        return saved;
    }

    @Override
    public List<ReciboDetalleHistorico> findByReciboDetalleId(Long reciboDetalleId) {
        if (reciboDetalleId == null) return List.of();
        return repository.findByReciboDetalleId(reciboDetalleId);
    }

    @Override
    @Transactional
    public void copiarHistorico(Long origenId, Long destinoId) {
        if (origenId == null || destinoId == null) return;
        List<ReciboDetalleHistorico> historicosOrigen = repository.findByReciboDetalleId(origenId);
        
        if (!historicosOrigen.isEmpty()) {
            List<ReciboDetalleHistorico> nuevosHistoricos = historicosOrigen.stream()
                    .map(h -> ReciboDetalleHistorico.builder()
                            .reciboDetalleId(destinoId)
                            .fechaHora(h.getFechaHora())
                            .usuarioId(h.getUsuarioId())
                            .accion(h.getAccion())
                            .build())
                    .collect(Collectors.toList());
            repository.saveAll(nuevosHistoricos);
            logger.info("Se han copiado {} registros históricos desde recibo_detalle_id: {} hacia recibo_detalle_id: {}", 
                    nuevosHistoricos.size(), origenId, destinoId);
        }
    }
}
