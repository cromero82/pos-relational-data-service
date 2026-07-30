package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.EstablecimientoDto;
import com.infinitesoft.pos_relational_data_service.entities.Establecimiento;
import com.infinitesoft.pos_relational_data_service.repositories.EstablecimientoRepository;
import com.infinitesoft.pos_relational_data_service.services.EstablecimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstablecimientoServiceImpl implements EstablecimientoService {

    @Autowired
    private EstablecimientoRepository repository;

    @Override
    public EstablecimientoDto getActual() {
        return toDto(getActualEntity());
    }

    @Override
    public Establecimiento getActualEntity() {
        return repository.findFirstByActivoTrueOrderByIdAsc()
                .orElseGet(this::defaultEstablecimiento);
    }

    @Override
    @Transactional
    public EstablecimientoDto update(Long id, EstablecimientoDto dto) {
        Establecimiento entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Establecimiento no encontrado: " + id));
        if (dto.getRazonSocial() != null) {
            entity.setRazonSocial(dto.getRazonSocial());
        }
        if (dto.getNombreComercial() != null) {
            entity.setNombreComercial(dto.getNombreComercial());
        }
        entity.setNit(dto.getNit());
        entity.setDigitoVerificacion(dto.getDigitoVerificacion());
        if (dto.getRegimenTributario() != null) {
            entity.setRegimenTributario(dto.getRegimenTributario());
        }
        if (dto.getRegimenLeyendaImpresion() != null) {
            entity.setRegimenLeyendaImpresion(dto.getRegimenLeyendaImpresion());
        }
        entity.setDireccion(dto.getDireccion());
        entity.setTelefono(dto.getTelefono());
        entity.setEmail(dto.getEmail());
        if (dto.getManejoEstrictoCuentas() != null) {
            entity.setManejoEstrictoCuentas(dto.getManejoEstrictoCuentas());
        }
        return toDto(repository.save(entity));
    }

    private Establecimiento defaultEstablecimiento() {
        return Establecimiento.builder()
                .razonSocial("MI ESTABLECIMIENTO")
                .nombreComercial("MI TIENDA POS")
                .regimenTributario("NO_RESPONSABLE_IVA")
                .regimenLeyendaImpresion("Establecimiento NO RESPONSABLE DE IVA")
                .activo(true)
                .build();
    }

    private EstablecimientoDto toDto(Establecimiento e) {
        if (e == null) {
            return null;
        }
        return EstablecimientoDto.builder()
                .id(e.getId())
                .razonSocial(e.getRazonSocial())
                .nombreComercial(e.getNombreComercial())
                .nit(e.getNit())
                .digitoVerificacion(e.getDigitoVerificacion())
                .regimenTributario(e.getRegimenTributario())
                .regimenLeyendaImpresion(e.getRegimenLeyendaImpresion())
                .direccion(e.getDireccion())
                .telefono(e.getTelefono())
                .email(e.getEmail())
                .manejoEstrictoCuentas(e.getManejoEstrictoCuentas())
                .build();
    }
}
