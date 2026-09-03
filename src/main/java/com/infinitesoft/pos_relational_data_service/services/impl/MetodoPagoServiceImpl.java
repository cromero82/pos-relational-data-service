package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.MetodoPago;
import com.infinitesoft.pos_relational_data_service.repositories.MetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.services.MetodoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetodoPagoServiceImpl implements MetodoPagoService {

    @Autowired
    private MetodoPagoRepository repository;

    @Override
    public List<MetodoPago> findAll() {
        return repository.findAllByOrderByIdAsc();
    }

    @Override
    public List<MetodoPago> findForEgresos() {
        return repository.findByVisiblePagosEgresosTrueOrderByIdAsc();
    }

    @Override
    public List<MetodoPago> findForTickets() {
        return repository.findByVisiblePagoTicketsTrueOrderByIdAsc();
    }

    @Override
    public List<MetodoPago> findQuePermitenNotificacion() {
        return repository.findByPermiteNotificacionTrueOrderByIdAsc();
    }

    @Override
    public MetodoPago findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public MetodoPago create(MetodoPago body) {
        validarBasico(body);
        body.setId(null);
        if (body.getEstado() == null || body.getEstado().isBlank()) {
            body.setEstado("ACTIVO");
        }
        if (body.getVisiblePagosEgresos() == null) {
            body.setVisiblePagosEgresos(true);
        }
        if (body.getVisiblePagoTickets() == null) {
            body.setVisiblePagoTickets(true);
        }
        if (body.getPermiteNotificacion() == null) {
            body.setPermiteNotificacion(false);
        }
        return repository.save(body);
    }

    @Override
    public MetodoPago update(Long id, MetodoPago body) {
        MetodoPago existing = findById(id);
        if (existing == null) {
            return null;
        }
        validarBasico(body);
        existing.setDescripcion(body.getDescripcion());
        existing.setDescripcionEgreso(body.getDescripcionEgreso());
        if (body.getEstado() != null && !body.getEstado().isBlank()) {
            existing.setEstado(body.getEstado());
        }
        existing.setFile(body.getFile());
        existing.setSigla(body.getSigla());
        existing.setColor(body.getColor());
        if (body.getVisiblePagosEgresos() != null) {
            existing.setVisiblePagosEgresos(body.getVisiblePagosEgresos());
        }
        if (body.getVisiblePagoTickets() != null) {
            existing.setVisiblePagoTickets(body.getVisiblePagoTickets());
        }
        if (body.getPermiteNotificacion() != null) {
            existing.setPermiteNotificacion(body.getPermiteNotificacion());
        }
        existing.setMonto(body.getMonto());
        if (body.getCodigoDianPaymentMeans() != null) {
            existing.setCodigoDianPaymentMeans(body.getCodigoDianPaymentMeans());
        }
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        MetodoPago existing = findById(id);
        if (existing == null) {
            return false;
        }
        // Soft-delete: evita romper FKs en historial/egresos/OF
        existing.setEstado("INACTIVO");
        existing.setVisiblePagoTickets(false);
        existing.setVisiblePagosEgresos(false);
        existing.setPermiteNotificacion(false);
        repository.save(existing);
        return true;
    }

    private void validarBasico(MetodoPago body) {
        if (body.getDescripcion() == null || body.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("descripcion es obligatoria");
        }
    }
}
