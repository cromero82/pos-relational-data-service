package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboElectronico;

public interface HistorialReciboElectronicoService {

    /**
     * Corrige el medio de un pendiente electrónico (CREADA/AMBIGUA):
     * HRE + líneas de pago / abono CxC (+ traslado OF si el dinero ya entró).
     */
    HistorialReciboElectronico corregirMetodoPago(Long historialElectronicoId, Long nuevoMetodoPagoId);
}
