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
     * Ambas patas de un traslado/distribución (mismo {@code grupoTrasladoId}), orden id ASC.
     */
    List<MovimientoOrigenFondosDto> findByGrupoTrasladoId(String grupoTrasladoId);

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

    /**
     * Movimientos de un tipo de ledger en rango de fecha de negocio.
     */
    List<MovimientoOrigenFondosDto> findPorTipo(
            String tipoMovimiento,
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

    /**
     * Revierte los traslados de distribución de efectivo (DISTRIBUCION) del corte:
     * siempre revierte la pata en la OF de método de pago (origen, p.ej. Caja: Efectivo).
     * La pata en Caja Menor / Caja General (destino) se revierte solo si esa OF NO tuvo
     * movimientos posteriores a la distribución; si los tuvo, se lanza
     * {@link IllegalStateException} y NO se revierte nada (transacción íntegra sin cambios).
     * Idempotente por corte. Uso: eliminación de corte (ver dividirCorte para SPLIT, que sí
     * permite revertir aunque haya movimientos posteriores, vía asiento puente).
     */
    void revertirDistribucionEfectivo(Long corteVentaId);

    /**
     * Verifica que el corte se pueda eliminar: NINGUNA de las OF que tocó (las de los medios de
     * pago con ventas y las cajas destino de la distribución) puede tener movimientos posteriores
     * a los del propio corte. Si los hay, lanza {@link IllegalStateException} y no se revierte
     * nada: la corrección debe hacerse con Dividir/Editar, que sí sabe manejar ese caso.
     */
    void validarCorteEliminable(Long corteVentaId);

    /**
     * SPLIT: revierte el corte original de forma incondicional (a diferencia de
     * {@link #revertirDistribucionEfectivo}), usando un asiento puente
     * ({@code AJUSTE_PUENTE_SPLIT}) en el/los OF destino (Caja Menor/General) para nunca dejar
     * saldo negativo aunque ya se haya gastado el dinero distribuido (egresos posteriores).
     * Orden: origen (crédito REVERSO_TRASLADO, luego débito REVERSO_ENTRADA_VENTA); destino
     * (crédito AJUSTE_PUENTE_SPLIT, luego débito REVERSO_TRASLADO). El puente queda abierto
     * hasta que {@link #cerrarPuenteSplit} lo consuma, una vez posteado el re-corte.
     * Observación uniforme: "Reverso por corrección #{correccionId} del corte #{corteVentaId}".
     */
    void revertirParaSplit(Long corteVentaId, Long correccionId);

    /**
     * SPLIT: cierra (consume) el asiento puente abierto por {@link #revertirParaSplit} para el
     * corte original, una vez que el re-corte ya posteó sus nuevas patas TRASLADO/entrada en el
     * OF destino (créditos que reemplazan económicamente el monto puenteado). Débito
     * AJUSTE_PUENTE_SPLIT por el mismo monto que se acreditó. Idempotente por corte.
     */
    void cerrarPuenteSplit(Long corteVentaId, Long correccionId);

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
