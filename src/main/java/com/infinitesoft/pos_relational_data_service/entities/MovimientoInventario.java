package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "movimiento_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String consecutivo;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "tipo_movimiento_id", nullable = false)
    private Long tipoMovimientoId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "CONFIRMADA";

    @Column(name = "fecha_hecho", nullable = false)
    private LocalDateTime fechaHecho;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "documento_venta_id")
    private Long documentoVentaId;

    @Column(name = "nota_ajuste_id")
    private Long notaAjusteId;

    @Column(name = "entrada_inventario_id")
    private Long entradaInventarioId;

    @Column(name = "historial_recibo_id")
    private Long historialReciboId;

    @Column(name = "recibo_id")
    private Long reciboId;

    @Column(name = "egreso_id")
    private Long egresoId;

    @Column(name = "motivo_texto")
    private String motivoTexto;

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
        if (estado == null) {
            estado = "CONFIRMADA";
        }
    }
}
