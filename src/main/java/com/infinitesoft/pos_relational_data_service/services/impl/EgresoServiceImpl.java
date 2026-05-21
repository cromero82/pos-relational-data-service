package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.Egreso;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.services.EgresoService;
import com.infinitesoft.pos_relational_data_service.services.EstadisticaFinancieraService;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
public class EgresoServiceImpl implements EgresoService {

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private EstadisticaFinancieraService estadisticaFinancieraService;

    @Override
    public Egreso create(Egreso egreso) {
        log.info("Iniciando servicio EgresoServiceImpl - Método: create - Egreso: {}", egreso);
        
        LocalDate requestDate = egreso.getFecha();
        
        Egreso saved = egresoRepository.save(egreso);
        
        ajustarEstadisticas(requestDate);
        
        return saved;
    }

    @Override
    public Egreso update(Long id, Egreso egreso) {
        if (egresoRepository.existsById(id)) {
            egreso.setId(id);
            Egreso saved = egresoRepository.save(egreso);
            ajustarEstadisticas(egreso.getFecha());
            return saved;
        }
        return null;
    }

    @Override
    public boolean delete(Long id) {
        Egreso existente = findById(id);
        if (existente != null) {
            LocalDate fechaEgreso = existente.getFecha();
            egresoRepository.deleteById(id);
            ajustarEstadisticas(fechaEgreso);
            return true;
        }
        return false;
    }

    private void ajustarEstadisticas(LocalDate requestDate) {
        if (requestDate == null) {
            return;
        }
        LocalDate currentDate = DateUtils.obtenerFechaSistema().toLocalDate();
        
        if (requestDate.isBefore(currentDate)) {
            log.info("La fecha del egreso {} es menor a la fecha actual {}. Iniciando ajuste de estadísticas.", requestDate, currentDate);
            
            String valorTiempoDia = requestDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            log.info("Ajustando estadística de DÍA para: {}", valorTiempoDia);
            estadisticaFinancieraService.crearOActualizarEstadisticaSync(valorTiempoDia);
            
            java.time.YearMonth requestMonth = java.time.YearMonth.from(requestDate);
            java.time.YearMonth currentMonth = java.time.YearMonth.from(currentDate);
            if (requestMonth.isBefore(currentMonth)) {
                String valorTiempoMes = requestMonth.toString();
                log.info("Ajustando estadística de MES para: {}", valorTiempoMes);
                estadisticaFinancieraService.crearOActualizarEstadisticaSync(valorTiempoMes);
            }
            
            int requestYear = requestDate.getYear();
            int currentYear = currentDate.getYear();
            if (requestYear < currentYear) {
                String valorTiempoAnio = String.valueOf(requestYear);
                log.info("Ajustando estadística de AÑO para: {}", valorTiempoAnio);
                estadisticaFinancieraService.crearOActualizarEstadisticaSync(valorTiempoAnio);
            }
        }
    }

    @Override
    public List<Egreso> findAll() {
        return egresoRepository.findAll();
    }

    @Override
    public Page<Egreso> search(String descripcion, Long tipoEgresoId, Long proveedorId, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
        return egresoRepository.search(descripcion, tipoEgresoId, proveedorId, fechaInicio, fechaFin, pageable);
    }

    @Override
    public Page<Egreso> searchDescripciones(String descripcion, Pageable pageable) {
        return egresoRepository.searchDescripciones(descripcion, pageable);
    }

    @Override
    public Egreso findById(Long id) {
        return egresoRepository.findById(id).orElse(null);
    }

    @Override
    public List<Egreso> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return egresoRepository.findByFechaBetween(fechaInicio, fechaFin);
    }

    @Override
    public List<Egreso> findByProveedorId(Long proveedorId) {
        return egresoRepository.findByProveedorId(proveedorId);
    }

    @Override
    public List<Egreso> findByTipoEgresoId(Long tipoEgresoId) {
        return egresoRepository.findByTipoEgresoId(tipoEgresoId);
    }
}
