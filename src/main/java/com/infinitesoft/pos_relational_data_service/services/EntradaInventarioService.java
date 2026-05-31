package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.EntradaInventarioDetalleRequest;
import com.infinitesoft.pos_relational_data_service.dto.EntradaInventarioEstadoResumenDto;
import com.infinitesoft.pos_relational_data_service.dto.PrecioCompraPreviewDto;
import com.infinitesoft.pos_relational_data_service.entities.EntradaInventario;
import com.infinitesoft.pos_relational_data_service.entities.EntradaInventarioDetalle;

import java.math.BigDecimal;
import java.util.List;

public interface EntradaInventarioService {

    EntradaInventario obtenerOCrearPorEgreso(Long egresoId);

    EntradaInventario findById(Long id);

    EntradaInventario findByEgresoId(Long egresoId);

    List<EntradaInventarioEstadoResumenDto> resumenPorEgresoIds(List<Long> egresoIds);

    PrecioCompraPreviewDto previewPrecioCompra(Long productoId, BigDecimal precioCompraNuevo);

    EntradaInventarioDetalle agregarDetalle(Long entradaId, EntradaInventarioDetalleRequest request);

    EntradaInventarioDetalle actualizarDetalle(Long entradaId, Long detalleId, EntradaInventarioDetalleRequest request);

    EntradaInventario eliminarDetalle(Long entradaId, Long detalleId);

    EntradaInventario confirmar(Long entradaId);

    EntradaInventario anular(Long entradaId);
}
