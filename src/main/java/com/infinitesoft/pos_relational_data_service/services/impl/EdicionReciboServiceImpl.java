package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.EdicionRecibo;
import com.infinitesoft.pos_relational_data_service.entities.EdicionReciboMetodoPago;
import com.infinitesoft.pos_relational_data_service.repositories.EdicionReciboMetodoPagoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EdicionReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.EdicionReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EdicionReciboServiceImpl implements EdicionReciboService {

    @Autowired
    private EdicionReciboRepository repository;

    @Autowired
    private EdicionReciboMetodoPagoRepository edicionReciboMetodoPagoRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void persistMetodoPagoIds(Long edicionReciboId, List<Long> ids) {
        edicionReciboMetodoPagoRepository.deleteByEdicionReciboId(edicionReciboId);
        if (ids != null && !ids.isEmpty()) {
            for (Long mpId : ids) {
                edicionReciboMetodoPagoRepository.save(EdicionReciboMetodoPago.builder()
                        .edicionReciboId(edicionReciboId)
                        .metodoPagoId(mpId)
                        .build());
            }
        }
    }

    private List<Long> resolveMetodoPagoIds(EdicionRecibo er) {
        if (er.getMetodoPagoIds() != null && !er.getMetodoPagoIds().isEmpty()) {
            return er.getMetodoPagoIds();
        }
        if (er.getMetodoPagoId() != null) {
            return Collections.singletonList(er.getMetodoPagoId());
        }
        return Collections.emptyList();
    }

    private void populateMetodoPagoIds(List<EdicionRecibo> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> ids = list.stream().map(EdicionRecibo::getId).collect(Collectors.toSet());
        Map<Long, List<Long>> junctionMap = edicionReciboMetodoPagoRepository
                .findByEdicionReciboIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        EdicionReciboMetodoPago::getEdicionReciboId,
                        Collectors.mapping(EdicionReciboMetodoPago::getMetodoPagoId, Collectors.toList())
                ));
        list.forEach(er -> er.setMetodoPagoIds(junctionMap.getOrDefault(er.getId(), Collections.emptyList())));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public EdicionRecibo create(EdicionRecibo edicionRecibo) {
        EdicionRecibo saved = repository.save(edicionRecibo);
        List<Long> ids = resolveMetodoPagoIds(edicionRecibo);
        persistMetodoPagoIds(saved.getId(), ids);
        saved.setMetodoPagoIds(ids);
        return saved;
    }

    @Override
    public List<EdicionRecibo> findAll() {
        List<EdicionRecibo> list = repository.findAll();
        populateMetodoPagoIds(list);
        return list;
    }

    @Override
    public EdicionRecibo findById(Long id) {
        if (id == null) return null;
        Optional<EdicionRecibo> opt = repository.findById(id);
        if (opt.isEmpty()) return null;
        EdicionRecibo er = opt.get();
        er.setMetodoPagoIds(edicionReciboMetodoPagoRepository.findMetodoPagoIdsByEdicionReciboId(id));
        return er;
    }

    @Override
    public Optional<EdicionRecibo> findByReciboId(Long reciboId) {
        Optional<EdicionRecibo> opt = repository.findByReciboId(reciboId);
        opt.ifPresent(er -> er.setMetodoPagoIds(
                edicionReciboMetodoPagoRepository.findMetodoPagoIdsByEdicionReciboId(er.getId())));
        return opt;
    }

    @Override
    @Transactional
    public void deleteByReciboId(Long reciboId) {
        repository.findByReciboId(reciboId).ifPresent(edicion -> {
            edicionReciboMetodoPagoRepository.deleteByEdicionReciboId(edicion.getId());
            repository.delete(edicion);
        });
    }

    @Override
    @Transactional
    public EdicionRecibo update(Long id, EdicionRecibo edicionRecibo) {
        if (id == null) return null;
        Optional<EdicionRecibo> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        EdicionRecibo existing = existingOpt.get();

        existing.setReciboId(edicionRecibo.getReciboId());
        existing.setHistorialReciboId(edicionRecibo.getHistorialReciboId());
        existing.setClienteId(edicionRecibo.getClienteId());
        existing.setEstadoId(edicionRecibo.getEstadoId());
        existing.setSesionId(edicionRecibo.getSesionId());
        existing.setTotal(edicionRecibo.getTotal());
        existing.setMontoRecibido(edicionRecibo.getMontoRecibido());

        // Resolver lista de métodos de pago
        List<Long> newIds = resolveMetodoPagoIds(edicionRecibo);
        Long firstMetodoPagoId = newIds.isEmpty() ? null : newIds.get(0);
        existing.setMetodoPagoId(firstMetodoPagoId);

        EdicionRecibo saved = repository.save(existing);
        persistMetodoPagoIds(id, newIds);
        saved.setMetodoPagoIds(newIds);
        return saved;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        edicionReciboMetodoPagoRepository.deleteByEdicionReciboId(id);
        repository.deleteById(id);
        return true;
    }

    @Override
    public void saveMetodoPagoIds(Long edicionReciboId, List<Long> metodoPagoIds) {
        persistMetodoPagoIds(edicionReciboId, metodoPagoIds);
        // Actualizar columna de compatibilidad
        Long first = (metodoPagoIds != null && !metodoPagoIds.isEmpty()) ? metodoPagoIds.get(0) : null;
        repository.findById(edicionReciboId).ifPresent(er -> {
            er.setMetodoPagoId(first);
            repository.save(er);
        });
    }
}
