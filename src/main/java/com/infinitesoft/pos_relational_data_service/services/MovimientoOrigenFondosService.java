package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.dto.*;
import com.infinitesoft.pos_relational_data_service.entities.CorteVentaDetalle;
import com.infinitesoft.pos_relational_data_service.entities.Egreso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface MovimientoOrigenFondosService {
    BigDecimal calcularSaldo(Integer origenFondosId);
    List<MovimientoOrigenFondosDto> findByOrigen(Integer origenFondosId);

    /**
     * Candidatos a formalizar egreso: movimiento por identificar en las bolsas dadas,
     * mismo valor, aún sin egreso vinculado.
     */
    List<MovimientoOrigenFondosDto> findCandidatosFormalizarEgreso(
            List<Integer> origenFondosIds,
            BigDecimal valor
    );

    /**
     * Movimientos con clasificación operativa en rango de fechas (impacto &gt; 0).
     */
    List<MovimientoOrigenFondosDto> findPorClasificacion(
            String clasificacionOperativa,
            LocalDate desde,
            LocalDate hasta
    );

    MovimientoOrigenFondosDto registrarEntradaManual(MovimientoEntradaRequest request);

    /**
     * Cobranza CxC: entrada a OF del medio, origenTipo ABONO_CXC.
     * No usa {@code ENTRADA_VENTA} (cobranza ≠ ventas del día).
     */
    MovimientoOrigenFondosDto registrarEntradaCobranza(
            Integer origenFondosId,
            java.math.BigDecimal monto,
            Long abonoCxcId,
            String terceroNombre,
            String observacion
    );

    /**
     * Entrada de instalación: define la Base del primer corte (origenTipo BASE_INICIAL).
     */
    MovimientoOrigenFondosDto registrarBaseInicial(BaseInicialRequest request);

    MovimientoOrigenFondosDto registrarPrestamo(MovimientoPrestamoRequest request);
    List<MovimientoOrigenFondosDto> registrarTraslado(MovimientoTrasladoRequest request);

    /**
     * Traslado etiquetado como distribución de un corte (entra en el watermark del corte).
     */
    List<MovimientoOrigenFondosDto> registrarTrasladoDistribucion(
            Integer origenFondosId,
            Integer origenDestinoId,
            BigDecimal valor,
            Long corteVentaId,
            String observacion
    );

    /**
     * Contabiliza en el ledger las ventas del periodo del corte (por medio → origen de fondos).
     * Idempotente por corte (origenTipo CORTE_VENTA).
     */
    void registrarEntradasVentaCorte(Long corteVentaId, List<CorteVentaDetalle> detalles);

    void revertirEntradasVentaCorte(Long corteVentaId);

    MovimientoOrigenFondosDto registrarAjuste(MovimientoAjusteRequest request);
    MovimientoOrigenFondosDto registrarAjusteCierre(
            Long metodoPagoId,
            BigDecimal totalSistema,
            BigDecimal totalReal,
            Integer motivoMovimientoId,
            Long corteVentaId,
            String observacion
    );
    void revertirAjustesCierre(Long corteVentaId);
    MovimientoOrigenFondosDto registrarSalidaEgreso(Egreso egreso);
    void revertirMovimientosEgreso(Long egresoId, String observacion);
    void sincronizarSalidaEgreso(Egreso anterior, Egreso actualizado);
}
