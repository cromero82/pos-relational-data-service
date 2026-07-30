package com.infinitesoft.pos_relational_data_service.controllers;

import com.infinitesoft.pos_relational_data_service.dto.MotivoMovimientoDto;
import com.infinitesoft.pos_relational_data_service.entities.MotivoMovimiento;
import com.infinitesoft.pos_relational_data_service.repositories.MotivoMovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('admin','cajero')")
@RequestMapping("/motivos-movimiento")
@CrossOrigin(origins = "*")
public class MotivoMovimientoController {

    @Autowired
    private MotivoMovimientoRepository repository;

    @GetMapping
    public List<MotivoMovimientoDto> findActivos() {
        return repository.findByActivoTrueOrderByOrdenAscIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private MotivoMovimientoDto toDto(MotivoMovimiento m) {
        return MotivoMovimientoDto.builder()
                .id(m.getId())
                .codigo(m.getCodigo())
                .nombre(m.getNombre())
                .categoria(m.getCategoria())
                .sistema(m.getSistema())
                .activo(m.getActivo())
                .orden(m.getOrden())
                .build();
    }
}
