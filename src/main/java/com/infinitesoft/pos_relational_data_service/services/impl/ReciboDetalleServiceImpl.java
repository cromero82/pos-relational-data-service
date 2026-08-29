package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleResponse;
import com.infinitesoft.pos_relational_data_service.entities.ProductoPresentacion;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import com.infinitesoft.pos_relational_data_service.repositories.ProductoPresentacionRepository;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboRepository;
import com.infinitesoft.pos_relational_data_service.security.dto.AuthUserDto;
import com.infinitesoft.pos_relational_data_service.security.service.AuthValidationService;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboDetalleRepository;
import com.infinitesoft.pos_relational_data_service.security.util.SecurityContextHelper;
import com.infinitesoft.pos_relational_data_service.services.ProductoPresentacionService;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleHistoricoService;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReciboDetalleServiceImpl implements ReciboDetalleService {

    @Autowired
    private ReciboDetalleRepository repository;

    @Autowired
    private ReciboRepository reciboRepository;

    @Autowired
    private AuthValidationService authValidationService;

    @Autowired
    private ReciboDetalleHistoricoService historicoService;

    @Autowired
    private ProductoPresentacionService presentacionService;

    @Autowired
    private ProductoPresentacionRepository presentacionRepository;

    @Override
    @Transactional
    public ReciboDetalleResponse create(ReciboDetalle detalle) {
        if (detalle.getUsuarioCreacion() == null) {
            detalle.setUsuarioCreacion(SecurityContextHelper.getUserId());
        }
        presentacionService.applyPresentacionToDetalle(detalle);
        ReciboDetalle saved = repository.save(detalle);

        boolean copiedFromParent = false;
        if (saved.getReciboId() != null) {
            Optional<Recibo> reciboOpt = reciboRepository.findById(saved.getReciboId());
            if (reciboOpt.isPresent()) {
                Recibo recibo = reciboOpt.get();
                if (recibo.getReciboPadreId() != null) {
                    List<ReciboDetalle> detallesPadre = repository.findByReciboIdOrderByIdAsc(recibo.getReciboPadreId());
                    Optional<ReciboDetalle> matchingDetallePadre = detallesPadre.stream()
                            .filter(d -> sameLineIdentity(d, saved))
                            .findFirst();
                    if (matchingDetallePadre.isPresent()) {
                        historicoService.copiarHistorico(matchingDetallePadre.get().getId(), saved.getId());
                        copiedFromParent = true;
                    }
                }
            }
        }

        if (!copiedFromParent) {
            historicoService.registrarAccion(saved.getId(), String.valueOf(saved.getUsuarioCreacion()), "agrega");
        }

        return toResponse(saved);
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
            if (dto.getPresentacionId() != null) {
                presentacionRepository.findById(dto.getPresentacionId()).ifPresent(dto::setPresentacion);
            }
            if (dto.getProducto() != null) {
                dto.getProducto().setPresentaciones(
                        presentacionService.listByProductoId(dto.getProductoId(), true));
            }
        }

        return dtos;
    }

    @Override
    public List<ReciboDetalle> findEntityListByReciboId(Long reciboId) {
        if (reciboId == null) return List.of();
        return repository.findByReciboIdOrderByIdAsc(reciboId);
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
        if (detalle.getPresentacionId() != null) {
            existing.setPresentacionId(detalle.getPresentacionId());
        }
        existing.setCantidad(detalle.getCantidad());
        existing.setSubtotal(detalle.getSubtotal());
        if (detalle.getPrecioUnitarioSnapshot() != null) {
            existing.setPrecioUnitarioSnapshot(detalle.getPrecioUnitarioSnapshot());
        }
        presentacionService.applyPresentacionToDetalle(existing);

        if (detalle.getFechaCreacion() != null) {
            existing.setFechaCreacion(detalle.getFechaCreacion());
        }
        if (detalle.getUsuarioCreacion() != null) {
            existing.setUsuarioCreacion(detalle.getUsuarioCreacion());
        }
        ReciboDetalle updated = repository.save(existing);

        if (newCantidad > oldCantidad) {
            historicoService.registrarAccion(updated.getId(), String.valueOf(SecurityContextHelper.getUserId()), "agrega");
        } else if (newCantidad < oldCantidad) {
            historicoService.registrarAccion(updated.getId(), String.valueOf(SecurityContextHelper.getUserId()), "elimina");
        }

        updated.setHistoricoAcciones(historicoService.findByReciboDetalleId(updated.getId()));
        return updated;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (id == null) return false;
        Optional<ReciboDetalle> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) return false;

        historicoService.registrarAccion(id, String.valueOf(SecurityContextHelper.getUserId()), "elimina");

        repository.deleteById(id);
        return true;
    }

    private ReciboDetalleResponse toResponse(ReciboDetalle saved) {
        ReciboDetalleResponse response = ReciboDetalleResponse.builder()
                .id(saved.getId())
                .reciboId(saved.getReciboId())
                .productoId(saved.getProductoId())
                .presentacionId(saved.getPresentacionId())
                .cantidad(saved.getCantidad())
                .cantidadBase(saved.getCantidadBase())
                .precioUnitarioSnapshot(saved.getPrecioUnitarioSnapshot())
                .factorSnapshot(saved.getFactorSnapshot())
                .subtotal(saved.getSubtotal())
                .fechaCreacion(saved.getFechaCreacion())
                .usuarioCreacion(saved.getUsuarioCreacion())
                .historicoAcciones(historicoService.findByReciboDetalleId(saved.getId()))
                .build();

        if (saved.getPresentacionId() != null) {
            ProductoPresentacion pp = presentacionService.findById(saved.getPresentacionId());
            response.setPresentacion(pp);
        }

        if (saved.getUsuarioCreacion() != null) {
            AuthUserDto userInfo = authValidationService.fetchUserInfoById(saved.getUsuarioCreacion());
            if (userInfo != null) {
                response.setNombreUsuarioAtendio(userInfo.getNombre());
            }
        }
        return response;
    }

    private static boolean sameLineIdentity(ReciboDetalle a, ReciboDetalle b) {
        if (a.getPresentacionId() != null && b.getPresentacionId() != null) {
            return a.getPresentacionId().equals(b.getPresentacionId());
        }
        return a.getProductoId() != null && a.getProductoId().equals(b.getProductoId());
    }
}
