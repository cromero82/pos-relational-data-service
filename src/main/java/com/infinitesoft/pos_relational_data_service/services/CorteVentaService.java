package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.BaseInicialPendienteDto;
import com.infinitesoft.pos_relational_data_service.dto.BaseInicialRequest;
import com.infinitesoft.pos_relational_data_service.dto.BaseInicialResultDto;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaDTO;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoRequest;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoResponse;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoPendienteDto;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoRequest;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionEfectivoResultDto;
import com.infinitesoft.pos_relational_data_service.dto.DividirCorteRequest;
import com.infinitesoft.pos_relational_data_service.dto.DividirCorteResultDto;
import com.infinitesoft.pos_relational_data_service.dto.DistribucionOriginalCorteDto;
import com.infinitesoft.pos_relational_data_service.dto.FinalizarRevisionCorteRequest;
import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;

import java.time.LocalDateTime;
import java.util.List;

public interface CorteVentaService {
    CorteVenta create(CorteVenta corteVenta);
    CorteVenta createFromDTO(CorteVentaDTO dto);
    List<CorteVenta> findAll();
    CorteVenta findById(Long id);
    CorteVenta update(Long id, CorteVenta corteVenta);
    boolean delete(Long id);
    CorteVentaDTO finalizarRevision(Long id, FinalizarRevisionCorteRequest request);
    CorteVentaRangoResponse consultarRango(CorteVentaRangoRequest request);
    List<CorteVentaDTO> search(LocalDateTime fechaIni, LocalDateTime fechaFin);
    List<CorteVentaDTO> findByIds(List<Long> ids);
    DistribucionEfectivoPendienteDto obtenerDistribucionPendiente();
    DistribucionEfectivoResultDto confirmarDistribucionEfectivo(Long corteId, DistribucionEfectivoRequest request);
    BaseInicialPendienteDto obtenerBaseInicialPendiente();
    BaseInicialResultDto confirmarBaseInicial(BaseInicialRequest request);
    CorteVentaDTO convertToDTO(CorteVenta entity);
    CorteVenta convertToEntity(CorteVentaDTO dto);

    /**
     * Corrige un corte mal generado: con 1 sola partición (mismo rango) actúa como editor
     * (solo deja traza, tipo EDICION, sin tocar el ledger); con 2+ particiones ejecuta el SPLIT
     * completo (reverso + puente + re-corte por partición), ver
     * {@code MovimientoOrigenFondosService#revertirParaSplit}. Motivo obligatorio en ambos casos.
     */
    DividirCorteResultDto dividirCorte(Long corteId, DividirCorteRequest request);

    /**
     * Monto que el corte originalmente distribuyó a Caja Menor / Caja General. Usado por el FE
     * del asistente Dividir para prellenar y validar el prorrateo entre particiones.
     */
    DistribucionOriginalCorteDto obtenerDistribucionOriginal(Long corteId);
}
