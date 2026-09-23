package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.EstablecimientoDto;
import com.infinitesoft.pos_relational_data_service.entities.Establecimiento;

public interface EstablecimientoService {

    EstablecimientoDto getActual();

    Establecimiento getActualEntity();

    EstablecimientoDto update(Long id, EstablecimientoDto dto);
}
