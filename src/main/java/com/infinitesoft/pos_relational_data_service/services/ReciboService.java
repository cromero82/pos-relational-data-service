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
}
