package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Recibo;

import java.util.List;

public interface ReciboService {
    Recibo create(Recibo recibo);
    Recibo saveAndFlush(Recibo recibo);
    List<Recibo> findAll();
    Recibo findById(Long id);
    Recibo update(Long id, Recibo recibo);
    boolean delete(Long id);
}
