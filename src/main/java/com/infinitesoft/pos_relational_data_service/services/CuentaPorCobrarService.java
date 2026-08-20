package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.AbonoCxcDto;
import com.infinitesoft.pos_relational_data_service.dto.AbrirCuentaPorCobrarRequest;
import com.infinitesoft.pos_relational_data_service.dto.CuentaPorCobrarDto;
import com.infinitesoft.pos_relational_data_service.dto.RegistrarAbonoCxcRequest;

import java.util.List;

public interface CuentaPorCobrarService {

    CuentaPorCobrarDto abrirDesdeTicket(AbrirCuentaPorCobrarRequest request);

    CuentaPorCobrarDto findById(Long id);

    List<CuentaPorCobrarDto> listarVigentes();

    List<CuentaPorCobrarDto> listarArchivados();

    CuentaPorCobrarDto findVigentePorRecibo(Long reciboId);

    CuentaPorCobrarDto findVigentePorTicket(Long ticketId);

    AbonoCxcDto registrarAbono(Long cuentaId, RegistrarAbonoCxcRequest request);

    List<AbonoCxcDto> listarAbonos(Long cuentaId);
}
