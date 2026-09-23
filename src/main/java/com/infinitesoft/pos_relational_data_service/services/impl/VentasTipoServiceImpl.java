package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import com.infinitesoft.pos_relational_data_service.repositories.VentasTipoRepository;
import com.infinitesoft.pos_relational_data_service.services.VentasTipoService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class VentasTipoServiceImpl implements VentasTipoService {

    @Autowired
    private VentasTipoRepository repository;

    @Override
    public VentasTipo create(VentasTipo ventasTipo) {
        log.info("Iniciando servicio VentasTipoServiceImpl - Método: create");
        
        // Lógica personalizada de creación
        Optional<VentasTipo> ultimoRegistroOpt = repository.findFirstByOrderByIdDesc();
        if (ultimoRegistroOpt.isPresent()) {
            VentasTipo ultimoRegistro = ultimoRegistroOpt.get();
            Long previoCorteVentaId = (ultimoRegistro.getCorteVentaId() != null ? ultimoRegistro.getCorteVentaId() : 0L) + 1;
            
            List<VentasTipo> registrosSiguientes = repository.findByIdGreaterThanEqual(previoCorteVentaId);
            
            BigDecimal sumaTotal = registrosSiguientes.stream()
                    .map(VentasTipo::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            ventasTipo.setTotalSistema(sumaTotal);
            
            if (!registrosSiguientes.isEmpty()) {
                ventasTipo.setCorteVentaId(registrosSiguientes.get(registrosSiguientes.size() - 1).getId());
            } else {
                ventasTipo.setCorteVentaId(ultimoRegistro.getCorteVentaId());
            }
        } else {
            // Primer registro, inicialización por defecto si no hay registros previos
            ventasTipo.setTotalSistema(ventasTipo.getTotal());
        }

        return repository.save(ventasTipo);
    }

    @Override
    public List<VentasTipo> findAll() {
        log.info("Iniciando servicio VentasTipoServiceImpl - Método: findAll");
        return repository.findAll();
    }

    @Override
    public VentasTipo findById(Long id) {
        log.info("Iniciando servicio VentasTipoServiceImpl - Método: findById para ID: {}", id);
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public VentasTipo update(Long id, VentasTipo ventasTipo) {
        log.info("Iniciando servicio VentasTipoServiceImpl - Método: update para ID: {}", id);
        if (id == null) return null;
        Optional<VentasTipo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        VentasTipo existing = existingOpt.get();
        existing.setMetodoPagoId(ventasTipo.getMetodoPagoId());
        existing.setTotal(ventasTipo.getTotal());
        existing.setTotalSistema(ventasTipo.getTotalSistema());
        existing.setCorteVentaId(ventasTipo.getCorteVentaId());
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        log.info("Iniciando servicio VentasTipoServiceImpl - Método: delete para ID: {}", id);
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
