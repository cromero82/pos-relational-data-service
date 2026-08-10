package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "metodo_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "descripcion_egreso", length = 100)
    private String descripcionEgreso;

    @Column(nullable = false)
    private String estado;

    @Column(name = "file", length = 300)
    private String file;

    @Column(name = "sigla", length = 10)
    private String sigla;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "visible_pagos_egresos", nullable = false)
    @Builder.Default
    private Boolean visiblePagosEgresos = true;

    @Column(name = "visible_pago_tickets", nullable = false)
    @Builder.Default
    private Boolean visiblePagoTickets = true;

    @Column(name = "monto", precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "plantilla_notificacion_pago", columnDefinition = "TEXT")
    private String plantillaNotificacionPago;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        String descripcionEgresoOriginal = this.descripcionEgreso;
        String plantillaOriginal = this.plantillaNotificacionPago;
        StringUtils.convertStringsToUpperCase(this);
        this.descripcionEgreso = descripcionEgresoOriginal;
        this.plantillaNotificacionPago = plantillaOriginal;
    }
}
