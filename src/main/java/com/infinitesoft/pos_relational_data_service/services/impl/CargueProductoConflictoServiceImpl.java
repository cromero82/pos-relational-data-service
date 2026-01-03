package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.CargueProducto;
import com.infinitesoft.pos_relational_data_service.entities.CargueProductoConflicto;
import com.infinitesoft.pos_relational_data_service.repositories.CargueProductoConflictoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.CargueProductoRepository;
import com.infinitesoft.pos_relational_data_service.services.CargueProductoConflictoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CargueProductoConflictoServiceImpl implements CargueProductoConflictoService {

    @Autowired
    private CargueProductoConflictoRepository repository;

    @Autowired
    private CargueProductoRepository cargueProductoRepository;

    @Override
    public List<CargueProductoConflicto> getAll(Integer cargueProductoId, Boolean unicamenteNoResueltos) {
        if (cargueProductoId != null) {
            if (unicamenteNoResueltos != null && unicamenteNoResueltos == true) {
                return repository.findByCargueProductoIdAndResuelto(cargueProductoId, false);
            }
            return repository.findByCargueProductoId(cargueProductoId);
        }
        // If cargueProductoId is null, we could support filtering by unicamenteNoResueltos globally,
        // but the controller requires cargueProductoId.
        // For safety, if unicamenteNoResueltos is provided but no ID, we could filter all (not requested but good practice)
        // However, sticking to current logic where ID is mandatory in controller.
        return repository.findAll();
    }

    @Override
    public CargueProductoConflicto create(CargueProductoConflicto cargueProductoConflicto) {
        return repository.save(cargueProductoConflicto);
    }

    @Override
    @Transactional
    public CargueProductoConflicto resolverConflicto(Integer cargueProductoConflictoId) {
        Optional<CargueProductoConflicto> conflictoOpt = repository.findById(cargueProductoConflictoId);
        if (conflictoOpt.isEmpty()) {
            throw new RuntimeException("Conflicto no encontrado con ID: " + cargueProductoConflictoId);
        }

        CargueProductoConflicto conflicto = conflictoOpt.get();

        if (Boolean.FALSE.equals(conflicto.getResuelto())) {
            conflicto.setResuelto(true);
            repository.save(conflicto);

            CargueProducto cargue = conflicto.getCargueProducto();
            if (cargue != null) {
                Integer resueltos = cargue.getTotalConflictosResultos();
                cargue.setTotalConflictosResultos(resueltos == null ? 1 : resueltos + 1);
                cargueProductoRepository.save(cargue);
            }
        }

        return conflicto;
    }
}
