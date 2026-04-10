package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalleHistorico;

import java.util.List;

public interface ReciboDetalleHistoricoService {
    ReciboDetalleHistorico registrarAccion(Long reciboDetalleId, String usuarioId, String accion);
    List<ReciboDetalleHistorico> findByReciboDetalleId(Long reciboDetalleId);
}
