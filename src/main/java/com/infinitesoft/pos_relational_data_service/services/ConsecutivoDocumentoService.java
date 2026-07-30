package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.enums.TipoConsecutivoDocumento;

public interface ConsecutivoDocumentoService {

    String nextConsecutivo(TipoConsecutivoDocumento tipo);
}
