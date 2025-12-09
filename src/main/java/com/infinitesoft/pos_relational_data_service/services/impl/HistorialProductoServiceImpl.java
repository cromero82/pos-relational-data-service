package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.HistorialProducto;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialProductoRepository;
import com.infinitesoft.pos_relational_data_service.services.HistorialProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HistorialProductoServiceImpl implements HistorialProductoService {

    @Autowired
    private HistorialProductoRepository repository;

    @Override
    public HistorialProducto create(HistorialProducto historial) {
        return repository.save(historial);
    }

    @Override
    public List<HistorialProducto> findAll() {
        return repository.findAll();
    }

    @Override
    public HistorialProducto findById(Long id) {
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public HistorialProducto update(Long id, HistorialProducto historial) {
        if (id == null) return null;
        Optional<HistorialProducto> existingOpt = repository.findById(id);
        if (!existingOpt.isPresent()) return null;
        HistorialProducto existing = existingOpt.get();
        existing.setProductoId(historial.getProductoId());
        existing.setEvento(historial.getEvento());
        existing.setPrecio(historial.getPrecio());
        existing.setActivo(historial.getActivo());
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
