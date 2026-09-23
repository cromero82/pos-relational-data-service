package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.GrupoEspejoDetalleResponse;
import com.infinitesoft.pos_relational_data_service.dto.GrupoEspejoRequest;
import com.infinitesoft.pos_relational_data_service.entities.GrupoEspejo;

import java.util.List;

public interface GrupoEspejoService {

    List<GrupoEspejoDetalleResponse> obtenerTodosConProductos(String query);

    GrupoEspejo crearGrupoEspejo(GrupoEspejoRequest request);

    GrupoEspejo agregarProducto(Long grupoEspejoId, Long productoId);

    void quitarProducto(Long grupoEspejoId, Long productoId);
    GrupoEspejo actualizarNombre(Long id, GrupoEspejoRequest request);
}
