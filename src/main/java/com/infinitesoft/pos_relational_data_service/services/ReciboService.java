package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboPagoLineaDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboPagoResponseDto;
import com.infinitesoft.pos_relational_data_service.dto.ReciboUpdateResult;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;

import java.util.List;

public interface ReciboService {
    Recibo create(ReciboDto reciboDto);
    Recibo saveAndFlush(Recibo recibo);
    List<Recibo> findAll();
    Recibo findById(Long id);
    ReciboUpdateResult update(Long id, ReciboDto reciboDto);
    boolean delete(Long id);

    /**
     * Opción B (liquidación CxC): archiva el recibo vivo a historial + documento VTA
     * con líneas de pago construidas desde abonos. No registra pendiente electrónico
     * ni cuenta como venta de corte (ver exclusión por {@code cuenta_por_cobrar.historial_recibo_id}).
     */
    ReciboPagoResponseDto liquidarComoVentaDesdeCxc(Long reciboId, List<ReciboPagoLineaDto> lineasPago);
}
