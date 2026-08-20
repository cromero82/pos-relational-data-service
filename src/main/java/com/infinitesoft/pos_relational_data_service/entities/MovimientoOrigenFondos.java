package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.entities.enums.TipoMovimientoOrigenFondos;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_origen_fondos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoOrigenFondos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_id", nullable = false, length = 36)
    private String usuarioId;

    @Column(name = "origen_fondos_id", nullable = false)
    private Integer origenFondosId;

    @Column(name = "origen_destino_id")
    private Integer origenDestinoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 40)
    private TipoMovimientoOrigenFondos tipoMovimiento;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal impacto;

    @Column(name = "saldo_antes", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoAntes;

    @Column(name = "saldo_despues", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoDespues;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "tercero_nombre", length = 150)
    private String terceroNombre;

    @Column(name = "motivo_movimiento_id")
    private Integer motivoMovimientoId;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "valor_sistema", precision = 14, scale = 2)
    private BigDecimal valorSistema;

    @Column(name = "valor_real", precision = 14, scale = 2)
    private BigDecimal valorReal;

    @Column(name = "origen_tipo", length = 80)
    private String origenTipo;

    /**
     * Id de la entidad de negocio asociada (egreso.id, corte_venta.id, …).
     * Null en movimientos sin referencia externa (traslados, BASE_INICIAL, etc.).
     */
    @Column(name = "id_referencia")
    private Long idReferencia;

    @Column(name = "grupo_traslado_id", length = 36)
    private String grupoTrasladoId;

    /**
     * CUENTA_PERSONAL | ANTICIPO_SALARIO | VALE_EMPLEADO | GASTO_NEGOCIO | OTRO_LEGALIZADO.
     * Tag de negocio (qué es) distinto del OF (dónde está la plata).
     */
    @Column(name = "clasificacion_operativa", length = 40)
    private String clasificacionOperativa;

    /** Reserva cierre de periodo operativo (mensual). Null = abierto. */
    @Column(name = "periodo_cierre_id")
    private Integer periodoCierreId;
}
