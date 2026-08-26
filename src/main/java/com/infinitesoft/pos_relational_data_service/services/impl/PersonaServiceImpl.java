package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Persona;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.PersonaRepository;
import com.infinitesoft.pos_relational_data_service.services.PersonaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    @Override
    public Persona create(Persona persona) {
        validarBasico(persona);
        personaRepository.findByDocumentoIgnoreCase(persona.getDocumento().trim()).ifPresent(x -> {
            throw new IllegalArgumentException("Ya existe una persona con documento " + persona.getDocumento());
        });
        if (persona.getActivo() == null) {
            persona.setActivo(true);
        }
        if (persona.getEsDuenoPropietario() == null) {
            persona.setEsDuenoPropietario(false);
        }
        return personaRepository.save(persona);
    }

    @Override
    public List<Persona> findAll() {
        return personaRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public List<Persona> findActivas() {
        return personaRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Override
    public Persona findById(Long id) {
        return personaRepository.findById(id).orElse(null);
    }

    @Override
    public Persona update(Long id, Persona persona) {
        Persona existing = findById(id);
        if (existing == null) {
            return null;
        }
        validarBasico(persona);
        personaRepository.findByDocumentoIgnoreCase(persona.getDocumento().trim()).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe una persona con documento " + persona.getDocumento());
            }
        });
        existing.setDocumento(persona.getDocumento());
        existing.setNombre(persona.getNombre());
        existing.setTelefono(persona.getTelefono());
        existing.setCorreo(persona.getCorreo());
        if (persona.getActivo() != null) {
            existing.setActivo(persona.getActivo());
        }
        if (persona.getEsDuenoPropietario() != null) {
            existing.setEsDuenoPropietario(persona.getEsDuenoPropietario());
        }
        return personaRepository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (!personaRepository.existsById(id)) {
            return false;
        }
        long enUso = egresoRepository.countByPersonaId(id);
        if (enUso > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar: hay " + enUso + " egreso(s) asociados a esta persona");
        }
        personaRepository.deleteById(id);
        return true;
    }

    private void validarBasico(Persona persona) {
        if (persona.getDocumento() == null || persona.getDocumento().isBlank()) {
            throw new IllegalArgumentException("documento es obligatorio");
        }
        if (persona.getNombre() == null || persona.getNombre().isBlank()) {
            throw new IllegalArgumentException("nombre es obligatorio");
        }
    }
}
