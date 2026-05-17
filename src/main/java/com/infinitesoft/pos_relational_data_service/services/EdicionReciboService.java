package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.EdicionRecibo;

import java.util.List;
import java.util.Optional;

public interface EdicionReciboService {
    EdicionRecibo create(EdicionRecibo edicionRecibo);
    List<EdicionRecibo> findAll();
    EdicionRecibo findById(Long id);
    Optional<EdicionRecibo> findByReciboId(Long reciboId);
    void deleteByReciboId(Long reciboId);
    EdicionRecibo update(Long id, EdicionRecibo edicionRecibo);
    boolean delete(Long id);

    /**
     * Reemplaza los métodos de pago asociados a la edición en edicion_recibo_metodo_pago.
     */
    void saveMetodoPagoIds(Long edicionReciboId, List<Long> metodoPagoIds);
}
