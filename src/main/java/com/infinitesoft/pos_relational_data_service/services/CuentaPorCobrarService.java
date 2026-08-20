package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.AbonoCxcDto;
import com.infinitesoft.pos_relational_data_service.dto.AbrirCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CerrarCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CuentaPorCobrarDto;
import com.infinitesoft.pos_relational_data_service.dto.RegistrarAbonoCxcRequest;
import com.infinitesoft.pos_relational_data_service.dto.SincronizarCxCTicketRequest;

import java.util.List;

public interface CuentaPorCobrarService {

    CuentaPorCobrarDto abrirDesdeTicket(AbrirCuentaPorCobrarRequest request);

    CuentaPorCobrarDto findById(Long id);

    List<CuentaPorCobrarDto> listarVigentes();

    List<CuentaPorCobrarDto> listarArchivados();

    CuentaPorCobrarDto findVigentePorRecibo(Long reciboId);

    CuentaPorCobrarDto findVigentePorTicket(Long ticketId);

    /**
     * Ajusta monto_original / saldo_pendiente cuando cambia el total del ticket
     * (agregar/quitar productos) mientras la CxC sigue vigente.
     */
    CuentaPorCobrarDto sincronizarTotalTicket(Long ticketId, SincronizarCxCTicketRequest request);

    AbonoCxcDto registrarAbono(Long cuentaId, RegistrarAbonoCxcRequest request);

    List<AbonoCxcDto> listarAbonos(Long cuentaId);

    /**
     * Anula CxC vigente solo si no hay abonos. El ticket sigue vivo para cobro normal.
     */
    CuentaPorCobrarDto anular(Long cuentaId, CerrarCuentaPorCobrarRequest request);

    /**
     * Castiga cartera: estado CASTIGADA, salida inventario a costo, cierra ticket.
     * No genera movimiento OF. Abonos previos (si los hay) se conservan.
     */
    CuentaPorCobrarDto castigar(Long cuentaId, CerrarCuentaPorCobrarRequest request);
}
