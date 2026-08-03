package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.OrigenFondosArbolItemDto;
import com.infinitesoft.pos_relational_data_service.dto.OrigenFondosDto;

import java.util.List;

public interface OrigenFondosService {
    List<OrigenFondosDto> findAllActivas();
    List<OrigenFondosDto> findParaEgreso();
    List<OrigenFondosArbolItemDto> findArbol();
    List<OrigenFondosArbolItemDto> findArbolParaEgreso();
    OrigenFondosDto findById(Integer id);

    /** Método de pago del O.F. o ancestro; {@code null} si la cuenta no tiene medio vinculado. */
    Long resolverMetodoPagoId(Integer origenFondosId);
}
