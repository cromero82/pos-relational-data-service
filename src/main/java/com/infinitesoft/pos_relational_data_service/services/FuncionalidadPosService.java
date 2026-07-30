package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.FuncionalidadPermisoDto;

import java.util.List;

public interface FuncionalidadPosService {
    List<FuncionalidadPermisoDto> misPermisos(List<String> rolSiglas);
}
