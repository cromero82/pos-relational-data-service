package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialRecibo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    @JsonIgnore
    private Client cliente;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado_id", nullable = false)
    private Long estadoId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @JsonAlias("sessionId")
    @Column(name = "sesion_id")
    private Long sesionId;


    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "monto_recibido", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoRecibido;

    @Column(name = "documento_venta_id")
    private Long documentoVentaId;

    /**
     * TRUE si se creó vía Ticket rápido (VARIOSPROD).
     * Ocultar en establecimientos RESPONSABLE_IVA.
     */
    @Column(name = "ticket_rapido", nullable = false)
    @Builder.Default
    private Boolean ticketRapido = false;

    @Transient
    private String documentoVentaConsecutivo;

    @Transient
    private Boolean restaurado;

    /** TRUE si el ticket tiene más de un medio en historial_recibo_pago (multipago). */
    @Transient
    private Boolean multipago;

    /**
     * Estado del pendiente electrónico (HRE) ligado a la venta, si existe.
     * Ej.: {@code CREADA} (pendiente de email), {@code CONFIRMADA} (con notificación).
     * Null = medio sin flujo de notificación (p.ej. efectivo).
     */
    @Transient
    private String estadoNotificacionElectronica;

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
    }
}
