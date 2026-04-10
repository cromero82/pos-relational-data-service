package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleResponse;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.service.AuthValidationService;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboDetalleRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleHistoricoService;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReciboDetalleServiceImpl implements ReciboDetalleService {

    @Autowired
    private ReciboDetalleRepository repository;

    @Autowired
    private AuthValidationService authValidationService;

    @Autowired
    private ReciboDetalleHistoricoService historicoService;

    @Override
    @Transactional
    public ReciboDetalleResponse create(ReciboDetalle detalle) {
        if (detalle.getUsuarioCreacion() == null) {
            detalle.setUsuarioCreacion(SecurityContextHelper.getUserId());
        }
        ReciboDetalle saved = repository.save(detalle);

        // Registro en el histórico
        historicoService.registrarAccion(saved.getId(), String.valueOf(saved.getUsuarioCreacion()), "agrega");
        
        ReciboDetalleResponse response = ReciboDetalleResponse.builder()
                .id(saved.getId())
                .reciboId(saved.getReciboId())
                .productoId(saved.getProductoId())
                .cantidad(saved.getCantidad())
                .subtotal(saved.getSubtotal())
                .fechaCreacion(saved.getFechaCreacion())
                .usuarioCreacion(saved.getUsuarioCreacion())
                .historicoAcciones(historicoService.findByReciboDetalleId(saved.getId()))
                .build();

        if (saved.getUsuarioCreacion() != null) {
            AuthUserDto userInfo = authValidationService.fetchUserInfoById(saved.getUsuarioCreacion());
            if (userInfo != null) {
                response.setNombreUsuarioAtendio(userInfo.getNombre());
            }
        }

        return response;
    }

    @Override
    public List<ReciboDetalle> findAll() {
        List<ReciboDetalle> list = repository.findAll();
        for (ReciboDetalle item : list) {
            item.setHistoricoAcciones(historicoService.findByReciboDetalleId(item.getId()));
        }
        return list;
    }

    @Override
    public ReciboDetalle findById(Long id) {
        if (id == null) return null;
        ReciboDetalle found = repository.findById(id).orElse(null);
        if (found != null) {
            found.setHistoricoAcciones(historicoService.findByReciboDetalleId(found.getId()));
        }
        return found;
    }

    @Override
    public List<ReciboDetalleDto> findByReciboId(Long reciboId) {
        if (reciboId == null) return List.of();
        List<ReciboDetalleDto> dtos = repository.findDtoByReciboId(reciboId);
        
        for (ReciboDetalleDto dto : dtos) {
            if (dto.getUsuarioCreacion() != null) {
                AuthUserDto userInfo = authValidationService.fetchUserInfoById(dto.getUsuarioCreacion());
                if (userInfo != null) {
                    dto.setNombreUsuarioAtendio(userInfo.getNombre());
                }
            }
            dto.setHistoricoAcciones(historicoService.findByReciboDetalleId(dto.getId()));
        }
        
        return dtos;
    }

    @Override
    public List<ReciboDetalle> findEntityListByReciboId(Long reciboId) {
        if (reciboId == null) return List.of();
        return repository.findByReciboId(reciboId);
    }

    @Override
    public long deleteByReciboId(Long reciboId) {
        if (reciboId == null) return 0L;
        return repository.deleteByReciboId(reciboId);
    }

    @Override
    @Transactional
    public ReciboDetalle update(Long id, ReciboDetalle detalle) {
        if (id == null) return null;
        Optional<ReciboDetalle> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return null;
        ReciboDetalle existing = existingOpt.get();

        int oldCantidad = existing.getCantidad() != null ? existing.getCantidad() : 0;
        int newCantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;

        existing.setReciboId(detalle.getReciboId());
        existing.setProductoId(detalle.getProductoId());
        existing.setCantidad(detalle.getCantidad());
        existing.setSubtotal(detalle.getSubtotal());
        // Preserve fechaCreacion and usuarioCreacion on update unless they are explicitly set in 'detalle'
        if (detalle.getFechaCreacion() != null) {
            existing.setFechaCreacion(detalle.getFechaCreacion());
        }
        if (detalle.getUsuarioCreacion() != null) {
            existing.setUsuarioCreacion(detalle.getUsuarioCreacion());
        }
        ReciboDetalle updated = repository.save(existing);

        // Registro en el histórico según la diferencia de cantidad
        if (newCantidad > oldCantidad) {
            historicoService.registrarAccion(updated.getId(), String.valueOf(SecurityContextHelper.getUserId()), "agrega");
        } else if (newCantidad < oldCantidad) {
            historicoService.registrarAccion(updated.getId(), String.valueOf(SecurityContextHelper.getUserId()), "elimina");
        }

        // Return updated object with history
        updated.setHistoricoAcciones(historicoService.findByReciboDetalleId(updated.getId()));
        return updated;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (id == null) return false;
        Optional<ReciboDetalle> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return false;
        
        // Registro en el histórico antes de eliminar
        historicoService.registrarAccion(id, String.valueOf(SecurityContextHelper.getUserId()), "elimina");
        
        repository.deleteById(id);
        return true;
    }
}
