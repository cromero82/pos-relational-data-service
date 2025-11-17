package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.FlujoDinero;
import com.infinitesoft.pos_relational_data_service.entities.enums.TipoFlujo;
import com.infinitesoft.pos_relational_data_service.repositories.FlujoDineroRepository;
import com.infinitesoft.pos_relational_data_service.services.FlujoDineroService;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class FlujoDineroServiceImpl implements FlujoDineroService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private FlujoDineroRepository repository;

    @Autowired
    private HistorialReciboService historialReciboService;

    @Override
    public FlujoDinero create(FlujoDinero flujo) {
        return repository.save(flujo);
    }

    @Override
    public List<FlujoDinero> findAll() {
        return repository.findAll();
    }

    @Override
    public FlujoDinero findById(Long id) {
        if (id == null) return null;
        Optional<FlujoDinero> opt = repository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public FlujoDinero update(Long id, FlujoDinero flujo) {
        if (id == null) return null;
        Optional<FlujoDinero> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        FlujoDinero existing = existingOpt.get();
        existing.setFecha(flujo.getFecha());
        existing.setTipoId(flujo.getTipoId());
        existing.setTotal(flujo.getTotal());
        existing.setUserId(flujo.getUserId());
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public List<FlujoDinero> findByFecha(LocalDate fecha) {
        if (fecha == null) return List.of();
        return repository.findAllByFecha(fecha);
    }

    @Override
    public List<FlujoDinero> findByFechaBetween(LocalDate from, LocalDate to, Integer tipoId) {
        if (from == null || to == null) return List.of();
        if (tipoId == null || tipoId <= 0) {
            return repository.findAllByFechaBetween(from, to);
        }
        return repository.findAllByFechaBetweenAndTipoId(from, to, tipoId);
    }

    @Override
    public List<FlujoDinero> searchAllDates(String dateInit, String dateEnd, Integer tipoId) {
        if (dateInit == null || dateEnd == null) return List.of();
        try {
            LocalDate from = LocalDate.parse(dateInit, DATE_FMT);
            LocalDate to = LocalDate.parse(dateEnd, DATE_FMT);
            return findByFechaBetween(from, to, tipoId);
        } catch (DateTimeParseException e) {
            return List.of();
        }
    }

    @Override
    public List<FlujoDinero> searchByWeeks(String interval, Integer tipoId) {
        if (interval == null || interval.isBlank()) return List.of();
        String s = interval.trim().toLowerCase(Locale.ROOT);
        try {
            LocalDate to = LocalDate.now();
            LocalDate from;
            if (s.endsWith("d")) { // days
                int days = Integer.parseInt(s.substring(0, s.length() - 1));
                from = to.minusDays(days);
            } else if (s.endsWith("w")) { // weeks
                int weeks = Integer.parseInt(s.substring(0, s.length() - 1));
                from = to.minusWeeks(weeks);
            } else if (s.endsWith("m")) { // months
                int months = Integer.parseInt(s.substring(0, s.length() - 1));
                from = to.minusMonths(months);
            } else {
                return List.of();
            }
            return findByFechaBetween(from, to, tipoId);
        } catch (NumberFormatException e) {
            return List.of();
        }
    }

    @Override
    public FlujoDinero generateIngresoFromHistorial(String fecha) {
        if (fecha == null) return null;
        try {
            LocalDate date = LocalDate.parse(fecha, DATE_FMT);
            BigDecimal totalDate = historialReciboService.getTotalByDate(fecha);

            FlujoDinero flujo = new FlujoDinero();
            flujo.setFecha(date);
            flujo.setTotal(totalDate);
            flujo.setTipoId(TipoFlujo.INGRESO.getId());
            return create(flujo);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
