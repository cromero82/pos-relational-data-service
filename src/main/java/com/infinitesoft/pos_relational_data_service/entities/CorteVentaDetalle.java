package com.infinitesoft.pos_relational_data_service.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "corte_venta_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteVentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corte_venta_id", nullable = false)
    private Long corteVentaId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "origen_fondos_id")
    private Integer origenFondosId;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal base = BigDecimal.ZERO;

    @Column(name = "total_ventas_sistema", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalVentasSistema = BigDecimal.ZERO;

    @Column(name = "total_egresos_sistema", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalEgresosSistema = BigDecimal.ZERO;

    @Column(name = "total_movimientos_sistema", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalMovimientosSistema = BigDecimal.ZERO;

    @Column(name = "total_sistema", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalSistema = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal total;

    @Column(precision = 14, scale = 2)
    private BigDecimal desfase;

    @Column(name = "motivo_desfase_id")
    private Integer motivoDesfaseId;

    @Column(name = "modo_captura", nullable = false, length = 25)
    @Builder.Default
    private String modoCaptura = "DECLARADO_CAJERO";

    @Column(name = "declarado_por", length = 36)
    private String declaradoPor;

    @Column(name = "revision_estado", nullable = false, length = 15)
    @Builder.Default
    private String revisionEstado = "PENDIENTE";

    @Column(name = "revision_comentario", length = 500)
    private String revisionComentario;

    @Column(name = "ajuste_generado", nullable = false)
    @Builder.Default
    private Boolean ajusteGenerado = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;
}
