package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recibo_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recibo_id", nullable = false)
    private Long reciboId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_id", insertable = false, updatable = false)
    @JsonIgnore
    private Recibo recibo;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", insertable = false, updatable = false)
    @JsonIgnore
    private Product producto;

    @Column(name = "presentacion_id")
    private Long presentacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentacion_id", insertable = false, updatable = false)
    @JsonIgnore
    private ProductoPresentacion presentacion;

    @Column(nullable = false)
    private Integer cantidad;

    /** Cantidad convertida a unidad base (cantidad * factor_snapshot). */
    @Column(name = "cantidad_base", precision = 14, scale = 4)
    private BigDecimal cantidadBase;

    @Column(name = "precio_unitario_snapshot", precision = 12, scale = 2)
    private BigDecimal precioUnitarioSnapshot;

    @Column(name = "factor_snapshot", precision = 12, scale = 4)
    private BigDecimal factorSnapshot;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creacion")
    private UUID usuarioCreacion;

    @Transient
    private List<ReciboDetalleHistorico> historicoAcciones;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = DateUtils.obtenerFechaSistema();
        }
    }
}
