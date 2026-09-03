package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;

import java.util.List;

public interface MetodoPagoService {
    List<MetodoPago> findAll();

    List<MetodoPago> findForEgresos();

    List<MetodoPago> findForTickets();

    List<MetodoPago> findQuePermitenNotificacion();

    MetodoPago findById(Long id);

    MetodoPago create(MetodoPago body);

    MetodoPago update(Long id, MetodoPago body);

    boolean delete(Long id);
}
