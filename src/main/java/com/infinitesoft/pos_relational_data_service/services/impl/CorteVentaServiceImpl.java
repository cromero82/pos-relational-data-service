package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.CorteVentaDTO;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoRequest;
import com.infinitesoft.pos_relational_data_service.dto.CorteVentaRangoResponse;
import com.infinitesoft.pos_relational_data_service.dto.VentasTipoDTO;
import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.VentasTipo;
import com.infinitesoft.pos_relational_data_service.repositories.CorteVentaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.CorteVentaService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CorteVentaServiceImpl implements CorteVentaService {

    @Autowired
    private CorteVentaRepository repository;

    @Autowired
    private HistorialReciboRepository historialReciboRepository;

    @Override
    public CorteVenta create(CorteVenta corteVenta) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: create");
        return repository.save(corteVenta);
    }

    @Override
    public CorteVenta createFromDTO(CorteVentaDTO dto) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: createFromDTO");
        CorteVenta entity = convertToEntity(dto);

        // Obtener usuarioId del contexto de seguridad si no viene en el DTO o para asegurar el valor correcto
        if (SecurityContextHelper.getUserId() != null) {
            entity.setUsuarioId(SecurityContextHelper.getUserId().toString());
        }

        if (dto.isUltimoCorte() && dto.isActual()) {
            CorteVentaRangoRequest request = CorteVentaRangoRequest.builder()
                    .ultimoCorte(true)
                    .actual(true)
                    .build();
            CorteVentaRangoResponse rango = this.consultarRango(request);

            entity.setFechaIni(rango.getFechaIni());
            entity.setFechaFin(rango.getFechaFin());
            entity.setTotalSistema(rango.getTotal());

            if (dto.getVentasTipo() != null) {
                for (VentasTipo vt : entity.getVentasTipo()) {
                    // Buscar el totalSistema calculado para este metodoPagoId
                    BigDecimal totalCalculado = rango.getVentasTipo().stream()
                            .filter(res -> res.getMetodoPagoId().equals(vt.getMetodoPagoId()))
                            .map(CorteVentaRangoResponse.VentasTipoResumenDTO::getTotalSistema)
                            .findFirst()
                            .orElse(BigDecimal.ZERO);
                    vt.setTotalSistema(totalCalculado);
                }
            }
        }

        // Calcular ultimoHistorialReciboId si fechaIni y fechaFin están presentes
        if (entity.getFechaIni() != null && entity.getFechaFin() != null) {
            historialReciboRepository.findMaxIdByFechaCreacionBetween(entity.getFechaIni(), entity.getFechaFin())
                    .ifPresent(entity::setUltimoHistorialReciboId);
        }

        return repository.save(entity);
    }

    @Override
    public List<CorteVenta> findAll() {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: findAll");
        return repository.findAll();
    }

    @Override
    public CorteVenta findById(Long id) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: findById para ID: {}", id);
        if (id == null) return null;
        return repository.findById(id).orElse(null);
    }

    @Override
    public CorteVenta update(Long id, CorteVenta corteVenta) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: update para ID: {}", id);
        if (id == null) return null;
        Optional<CorteVenta> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        
        CorteVenta existing = existingOpt.get();
        existing.setUsuarioId(corteVenta.getUsuarioId());
        existing.setFechaCreacion(corteVenta.getFechaCreacion());
        existing.setFechaIni(corteVenta.getFechaIni());
        existing.setFechaFin(corteVenta.getFechaFin());
        existing.setUltimoHistorialReciboId(corteVenta.getUltimoHistorialReciboId());
        existing.setTotal(corteVenta.getTotal());
        existing.setTotalSistema(corteVenta.getTotalSistema());
        existing.setVentasTipo(corteVenta.getVentasTipo());
        
        return repository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: delete para ID: {}", id);
        if (id == null) return false;
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public CorteVentaRangoResponse consultarRango(CorteVentaRangoRequest request) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: consultarRango");
        
        // Validaciones manuales según requerimiento
        if (!request.isUltimoCorte() && request.getFechaIni() == null) {
            throw new IllegalArgumentException("Si el campo 'ultimoCorte' es falso, 'fechaIni' no puede ser nulo.");
        }
        if (!request.isActual() && request.getFechaFin() == null) {
            throw new IllegalArgumentException("Si el campo 'actual' es falso, 'fechaFin' no puede ser nulo.");
        }

        CorteVentaRangoResponse response = new CorteVentaRangoResponse();
        List<CorteVentaDTO> otrosCortes = new ArrayList<>();

        if (request.isUltimoCorte()) {
            Optional<CorteVenta> ultimoCorteVentaOpt = repository.findFirstByOrderByFechaCreacionDesc();
            Optional<HistorialRecibo> posteriorHistorialRecibo;

            if (ultimoCorteVentaOpt.isPresent()) {
                CorteVenta ultimoCorteVenta = ultimoCorteVentaOpt.get();
                response.setUltimoCorte(ultimoCorteVenta.getFechaCreacion());
                
                // El requerimiento es: traer todos los registros (list) y luego aplicar el filtro > fechaCreacion
                // Se asegura de omitir registros truncando a MINUTOS tanto el recibo como la fecha fin del corte
                // para que si el corte terminó a las 19:22, ignore cualquier recibo de las 19:22:xx
                List<HistorialRecibo> posibles = historialReciboRepository.findAllPosteriorAFecha(ultimoCorteVenta.getFechaFin());
                
                LocalDateTime fechaFinMinutos = ultimoCorteVenta.getFechaFin().withSecond(0).withNano(0);
                
                posteriorHistorialRecibo = posibles.stream()
                        .filter(hr -> hr.getFechaCreacion().withSecond(0).withNano(0).isAfter(fechaFinMinutos))
                        .findFirst();

                // Si aún así no se encuentra (por ejemplo si ambos tienen el mismo segundo pero id 35 debe ser saltado)
                if (posteriorHistorialRecibo.isEmpty() && !posibles.isEmpty()) {
                    posteriorHistorialRecibo = posibles.stream()
                            .filter(hr -> hr.getId() > (ultimoCorteVenta.getUltimoHistorialReciboId() != null ? 
                                    ultimoCorteVenta.getUltimoHistorialReciboId() : 0))
                            .findFirst();
                }
            } else {
                posteriorHistorialRecibo = historialReciboRepository.findFirstByOrderByFechaCreacionAsc();
            }

            posteriorHistorialRecibo.ifPresent(hr -> response.setFechaIni(hr.getFechaCreacion()));
            
            if (request.isActual()) {
                Optional<HistorialRecibo> ultimoHistorialRecibo = historialReciboRepository.findFirstByOrderByFechaCreacionDesc();
                ultimoHistorialRecibo.ifPresent(hr -> response.setFechaFin(hr.getFechaCreacion()));
            } else {
                response.setFechaFin(request.getFechaFin());
            }
        } else {
            response.setFechaIni(request.getFechaIni());
            response.setFechaFin(request.getFechaFin());
            
            // Consultar cortes intersectados
            List<CorteVenta> intersectados = repository.findByFechaIniBetweenOrFechaFinBetween(
                    request.getFechaIni(), request.getFechaFin(),
                    request.getFechaIni(), request.getFechaFin());
            
            otrosCortes = intersectados.stream().map(this::convertToDTO).collect(Collectors.toList());
        }

        response.setOtrosCortesIntersectados(otrosCortes);

        // Consultar resumen de ventas por método de pago
        if (response.getFechaIni() != null && response.getFechaFin() != null) {
            List<Object[]> resumen = historialReciboRepository.findResumenVentasPorMetodoPago(response.getFechaIni(), response.getFechaFin());
            List<CorteVentaRangoResponse.VentasTipoResumenDTO> ventasTipo = resumen.stream()
                    .map(obj -> CorteVentaRangoResponse.VentasTipoResumenDTO.builder()
                            .metodoPagoId((Long) obj[0])
                            .totalSistema((BigDecimal) obj[1])
                            .build())
                    .collect(Collectors.toList());
            
            response.setVentasTipo(ventasTipo);
            
            BigDecimal total = ventasTipo.stream()
                    .map(CorteVentaRangoResponse.VentasTipoResumenDTO::getTotalSistema)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotal(total);
        } else {
            response.setVentasTipo(Collections.emptyList());
            response.setTotal(BigDecimal.ZERO);
        }

        return response;
    }

    @Override
    public List<CorteVentaDTO> search(LocalDateTime fechaIni, LocalDateTime fechaFin) {
        log.info("Iniciando servicio CorteVentaServiceImpl - Método: search");
        return repository.findByFechaIniGreaterThanEqualAndFechaIniLessThanEqual(fechaIni, fechaFin)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CorteVentaDTO convertToDTO(CorteVenta entity) {
        if (entity == null) return null;
        
        List<VentasTipoDTO> ventasTipoDTOList = entity.getVentasTipo() != null ?
                entity.getVentasTipo().stream()
                        .map(vt -> VentasTipoDTO.builder()
                                .id(vt.getId())
                                .metodoPagoId(vt.getMetodoPagoId())
                                .total(vt.getTotal())
                                .totalSistema(vt.getTotalSistema())
                                .corteVentaId(vt.getCorteVentaId())
                                .build())
                        .collect(Collectors.toList()) : Collections.emptyList();

        return CorteVentaDTO.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .fechaIni(entity.getFechaIni())
                .fechaFin(entity.getFechaFin())
                .ultimoHistorialReciboId(entity.getUltimoHistorialReciboId())
                .total(entity.getTotal())
                .totalSistema(entity.getTotalSistema())
                .ventasTipo(ventasTipoDTOList)
                .build();
    }

    @Override
    public CorteVenta convertToEntity(CorteVentaDTO dto) {
        if (dto == null) return null;

        List<VentasTipo> ventasTipoList = dto.getVentasTipo() != null ?
                dto.getVentasTipo().stream()
                        .map(vtDto -> VentasTipo.builder()
                                .id(vtDto.getId())
                                .metodoPagoId(vtDto.getMetodoPagoId())
                                .total(vtDto.getTotal())
                                .totalSistema(vtDto.getTotalSistema())
                                .corteVentaId(vtDto.getCorteVentaId())
                                .build())
                        .collect(Collectors.toList()) : null;

        return CorteVenta.builder()
                .id(dto.getId())
                .usuarioId(dto.getUsuarioId())
                .fechaIni(dto.getFechaIni())
                .fechaFin(dto.getFechaFin())
                .ultimoHistorialReciboId(dto.getUltimoHistorialReciboId())
                .total(dto.getTotal())
                .totalSistema(dto.getTotalSistema())
                .ventasTipo(ventasTipoList)
                .build();
    }
}
