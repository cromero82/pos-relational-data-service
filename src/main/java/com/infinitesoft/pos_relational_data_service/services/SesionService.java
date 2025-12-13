package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.dto.SesionDto;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface SesionService {
    // DTO-based API
    SesionDto create(SesionDto sesionDto, HttpServletRequest request);
    List<SesionDto> findAll(HttpServletRequest request);
    // Nuevo: obtener todas las sesiones del usuario (activas e inactivas)
    List<SesionDto> findAllByUserAllStates(HttpServletRequest request);
    SesionDto findById(Long id, HttpServletRequest request);
    SesionDto update(Long id, SesionDto sesionDto, HttpServletRequest request);
    boolean delete(Long id, HttpServletRequest request);

    // Overloads for internal use (legacy calls within services)
    Sesion findById(Long id);
    Sesion update(Long id, Sesion sesion);
}
