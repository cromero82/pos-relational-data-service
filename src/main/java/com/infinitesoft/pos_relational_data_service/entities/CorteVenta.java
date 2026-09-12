package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "corte_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, length = 36)
    private String usuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_ini", nullable = false)
    private LocalDateTime fechaIni;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(name = "ultimo_historial_recibo_id")
    private Long ultimoHistorialReciboId;

    /** Watermark del ledger: último movimiento incluido en este corte. */
    @Column(name = "ultimo_movimiento_origen_fondos_id")
    private Long ultimoMovimientoOrigenFondosId;

    /** PENDIENTE | CONFIRMADA */
    @Column(name = "distribucion_efectivo_estado", length = 20)
    private String distribucionEfectivoEstado;

    /** Saldo restante en Caja: Efectivo tras distribución (= Base próximo turno). */
    @Column(name = "base_siguiente_efectivo", precision = 14, scale = 2)
    private BigDecimal baseSiguienteEfectivo;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "total_sistema", precision = 12, scale = 2)
    private BigDecimal totalSistema;

    /** Σ tickets cobrados del corte. Dashboard Ingresos; no es Contado ni Esperado. */
    @Column(name = "total_ventas_sistema", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalVentasSistema = BigDecimal.ZERO;

    @Column(name = "sesion_id")
    private Long sesionId;

    @Column(name = "fondo_inicial_efectivo", precision = 12, scale = 2)
    private BigDecimal fondoInicialEfectivo;

    @Column(name = "motivo_desfase", columnDefinition = "TEXT")
    private String motivoDesfase;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "creada";

    @Column(length = 200)
    private String observacion;

    @Column(name = "revisado_por", length = 36)
    private String revisadoPor;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "corte_venta_id")
    private List<VentasTipo> ventasTipo;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
        if (fechaIni == null) {
            fechaIni = DateUtils.obtenerFechaSistema();
        }
        if (fechaFin == null) {
            fechaFin = DateUtils.obtenerFechaSistema();
        }
    }
}
