package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.HistorialPrecioProducto;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialPrecioProductoRepository;
import com.infinitesoft.pos_relational_data_service.services.HistorialPrecioProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistorialPrecioProductoServiceImpl implements HistorialPrecioProductoService {

    @Autowired
    private HistorialPrecioProductoRepository repository;

    @Override
    @Transactional
    public HistorialPrecioProducto save(HistorialPrecioProducto historial) {
        return repository.save(historial);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialPrecioProducto> findByProductoId(Long productoId) {
        return repository.findByProductoIdOrderByFechaCreacionDesc(productoId);
    }
}
