package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDto;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;

import java.util.List;

public interface ReciboService {
    Recibo create(ReciboDto reciboDto);
    Recibo saveAndFlush(Recibo recibo);
    List<Recibo> findAll();
    Recibo findById(Long id);
    Recibo update(Long id, ReciboDto reciboDto);
    boolean delete(Long id);

    /**
     * Reemplaza los métodos de pago asociados al recibo en la tabla recibo_metodo_pago.
     * También actualiza la columna de compatibilidad metodo_pago_id con el primero de la lista.
     */
    void saveMetodoPagoIds(Long reciboId, List<Long> metodoPagoIds);
}
