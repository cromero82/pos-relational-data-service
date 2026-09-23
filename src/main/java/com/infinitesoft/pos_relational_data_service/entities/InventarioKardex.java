package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventario_kardex")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioKardex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "movimiento_detalle_id", nullable = false)
    private Long movimientoDetalleId;

    @Column(name = "fecha_hecho", nullable = false)
    private LocalDateTime fechaHecho;

    @Column(name = "cantidad_entrada", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal cantidadEntrada = BigDecimal.ZERO;

    @Column(name = "cantidad_salida", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal cantidadSalida = BigDecimal.ZERO;

    @Column(name = "saldo_resultante", nullable = false, precision = 12, scale = 3)
    private BigDecimal saldoResultante;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaHecho == null) {
            fechaHecho = DateUtils.obtenerFechaSistema();
        }
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
        if (cantidadEntrada == null) {
            cantidadEntrada = BigDecimal.ZERO;
        }
        if (cantidadSalida == null) {
            cantidadSalida = BigDecimal.ZERO;
        }
    }
}
