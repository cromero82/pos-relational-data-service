package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cuenta_por_cobrar", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CuentaPorCobrar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "historial_recibo_id")
    private Long historialReciboId;

    @Column(name = "documento_venta_id")
    private Long documentoVentaId;

    @Column(name = "recibo_id")
    private Long reciboId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "fecha_origen", nullable = false)
    private LocalDateTime fechaOrigen;

    @Column(name = "monto_original", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoOriginal;

    @Column(name = "saldo_pendiente", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onPrePersist() {
        LocalDateTime now = DateUtils.obtenerFechaSistema();
        if (fechaOrigen == null) {
            fechaOrigen = now;
        }
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
        fechaActualizacion = now;
        if (estado == null || estado.isBlank()) {
            estado = "ABIERTA";
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        fechaActualizacion = DateUtils.obtenerFechaSistema();
    }
}
