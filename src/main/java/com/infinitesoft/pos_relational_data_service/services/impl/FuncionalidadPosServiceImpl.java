package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.FuncionalidadPermisoDto;
import com.infinitesoft.pos_relational_data_service.entities.FuncionalidadPos;
import com.infinitesoft.pos_relational_data_service.entities.FuncionalidadRol;
import com.infinitesoft.pos_relational_data_service.repositories.FuncionalidadRolRepository;
import com.infinitesoft.pos_relational_data_service.services.FuncionalidadPosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FuncionalidadPosServiceImpl implements FuncionalidadPosService {

    @Autowired
    private FuncionalidadRolRepository funcionalidadRolRepository;

    @Override
    public List<FuncionalidadPermisoDto> misPermisos(List<String> rolSiglas) {
        if (rolSiglas == null || rolSiglas.isEmpty()) {
            return Collections.emptyList();
        }

        List<FuncionalidadRol> asignaciones = funcionalidadRolRepository
                .findPermisosByRolSiglas(rolSiglas);

        Map<Integer, FuncionalidadPermisoDto> merged = new LinkedHashMap<>();
        for (FuncionalidadRol asignacion : asignaciones) {
            FuncionalidadPos funcionalidad = asignacion.getFuncionalidad();
            if (funcionalidad == null || Boolean.FALSE.equals(funcionalidad.getActivo())) {
                continue;
            }
            FuncionalidadPermisoDto existing = merged.get(funcionalidad.getId());
            if (existing == null) {
                merged.put(funcionalidad.getId(), FuncionalidadPermisoDto.builder()
                        .codigo(funcionalidad.getCodigo())
                        .nombre(funcionalidad.getNombre())
                        .seccion(funcionalidad.getSeccion())
                        .rutaFront(funcionalidad.getRutaFront())
                        .etiquetaMenu(funcionalidad.getEtiquetaMenu())
                        .badgeUi(funcionalidad.getBadgeUi())
                        .requiereDisclaimer(Boolean.TRUE.equals(funcionalidad.getRequiereDisclaimer()))
                        .puedeEscribir(Boolean.TRUE.equals(asignacion.getPuedeEscribir()))
                        .build());
            } else if (Boolean.TRUE.equals(asignacion.getPuedeEscribir())) {
                existing.setPuedeEscribir(true);
            }
        }

        return new ArrayList<>(merged.values()).stream()
                .sorted((a, b) -> a.getCodigo().compareToIgnoreCase(b.getCodigo()))
                .collect(Collectors.toList());
    }
}
