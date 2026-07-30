package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.EntradaInventario;
import com.infinitesoft.pos_relational_data_service.entities.EntradaInventarioDetalle;
import com.infinitesoft.pos_relational_data_service.entities.HistorialRecibo;
import com.infinitesoft.pos_relational_data_service.entities.HistorialReciboDetalle;
import com.infinitesoft.pos_relational_data_service.entities.NotaAjusteDocumento;

import java.util.List;
import java.util.UUID;

public interface MovimientoInventarioService {

    void registrarVentaPos(
            HistorialRecibo historial,
            Long documentoVentaId,
            List<HistorialReciboDetalle> detalles,
            UUID usuarioId);

    void registrarReintegroVenta(
            HistorialRecibo historial,
            NotaAjusteDocumento nota,
            List<HistorialReciboDetalle> detalles,
            UUID usuarioId);

    void registrarCompraEgresoTrazabilidad(
            EntradaInventario entrada,
            List<EntradaInventarioDetalle> detalles,
            UUID usuarioId);
}
