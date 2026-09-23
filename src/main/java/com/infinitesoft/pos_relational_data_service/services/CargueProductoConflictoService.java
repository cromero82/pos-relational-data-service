package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.CargueProductoConflicto;
import java.util.List;

public interface CargueProductoConflictoService {
    List<CargueProductoConflicto> getAll(Integer cargueProductoId, Boolean unicamenteNoResueltos);
    CargueProductoConflicto create(CargueProductoConflicto cargueProductoConflicto);
    CargueProductoConflicto resolverConflicto(Integer cargueProductoConflictoId);
}
