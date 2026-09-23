package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Línea de cobro de un historial_recibo (multipago).
 * SUM(monto) por historial debe igualar historial_recibo.total.
 */
@Entity
@Table(name = "historial_recibo_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialReciboPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "historial_recibo_id", nullable = false)
    private Long historialReciboId;

    @Column(name = "metodo_pago_id", nullable = false)
    private Long metodoPagoId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    @Builder.Default
    private Short orden = 1;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (orden == null) {
            orden = 1;
        }
    }
}
