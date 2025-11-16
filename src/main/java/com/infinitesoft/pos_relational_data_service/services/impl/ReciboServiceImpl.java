package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboDetalle;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import com.infinitesoft.pos_relational_data_service.entities.enums.ReciboEstado;
import com.infinitesoft.pos_relational_data_service.repositories.ReciboRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboDetalleRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.HistorialReciboService;
import com.infinitesoft.pos_relational_data_service.services.ReciboDetalleService;
import com.infinitesoft.pos_relational_data_service.services.ReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReciboServiceImpl implements ReciboService {

    @Autowired
    private ReciboRepository reciboRepository;

    @Autowired
    private HistorialReciboService historialReciboService;

    @Autowired
    private HistorialReciboDetalleRepository historialReciboDetalleRepository;

    @Autowired
    private ReciboDetalleService reciboDetalleService;

    @Autowired
    private TicketReciboRepository ticketReciboRepository;

    @Override
    public Recibo create(Recibo recibo) {
        // Default estado to PENDIENTE_PAGO when not provided
        if (recibo.getEstadoId() == null) {
            recibo.setEstadoId(ReciboEstado.PENDIENTE_PAGO.getId());
        }
        return reciboRepository.save(recibo);
    }

    @Override
    public List<Recibo> findAll() {
        return reciboRepository.findAll();
    }

    @Override
    public Recibo findById(Long id) {
        if (id == null) return null;
        Optional<Recibo> opt = reciboRepository.findById(id);
        return opt.orElse(null);
    }

    @Override
    @Transactional
    public Recibo update(Long id, Recibo recibo) {
        if (id == null) return null;
        Optional<Recibo> existingOpt = reciboRepository.findById(id);
        if (existingOpt.isEmpty()) return null;

        Recibo existing = existingOpt.get();
        // Update mutable fields, keep id and fechaCreacion
        existing.setClienteId(recibo.getClienteId());
        existing.setEstadoId(recibo.getEstadoId());
        existing.setMetodoPagoId(recibo.getMetodoPagoId());
        // Preserve existing sesionId if not provided in the update payload
        if (recibo.getSesionId() != null) {
            existing.setSesionId(recibo.getSesionId());
        }
        existing.setTotal(recibo.getTotal());

        // Determine target estado
        ReciboEstado targetEstado = ReciboEstado.fromId(existing.getEstadoId());
        if (targetEstado == ReciboEstado.PAGADO || targetEstado == ReciboEstado.ANULADO) {
            // 1) Copy Recibo to HistorialRecibo (using the just-updated fields)
            HistorialRecibo hist = HistorialRecibo.builder()
                    .clienteId(existing.getClienteId())
                    .estadoId(existing.getEstadoId())
                    .metodoPagoId(existing.getMetodoPagoId())
                    .sesionId(existing.getSesionId())
                    .total(existing.getTotal())
                    .build();
            HistorialRecibo savedHist = historialReciboService.create(hist);

            // 2) Copy all ReciboDetalle rows into HistorialReciboDetalle linked to savedHist.id
            List<ReciboDetalle> detalles = reciboDetalleService.findEntityListByReciboId(existing.getId());
            for (ReciboDetalle d : detalles) {
                HistorialReciboDetalle hd = HistorialReciboDetalle.builder()
                        .reciboId(savedHist.getId())
                        .productoId(d.getProductoId())
                        .cantidad(d.getCantidad())
                        .subtotal(d.getSubtotal())
                        .build();
                historialReciboDetalleRepository.save(hd);
            }

            // 3) Delete all items from recibo_detalle
            reciboDetalleService.deleteByReciboId(existing.getId());

            // 4) Delete from ticket_recibo using reciboId
            ticketReciboRepository.deleteByReciboId(existing.getId());

            // 5) Finally delete the recibo itself
            reciboRepository.deleteById(existing.getId());

            // Return null to signal resource removal to controller
            return null;
        }

        return reciboRepository.save(existing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) return false;
        if (!reciboRepository.existsById(id)) return false;
        reciboRepository.deleteById(id);
        return true;
    }
}
