package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_recibos_electronicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HistorialReciboElectronico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Venta QR; null si el origen es un abono CxC. */
    @Column(name = "historial_recibo_id")
    private Long historialReciboId;

    /** Abono CxC QR; null si el origen es una venta. */
    @Column(name = "abono_cxc_id")
    private Long abonoCxcId;

    @Column(name = "sesion_id")
    private Long sesionId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "monto_esperado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoEsperado;

    /** Monto del email al confirmar (puede diferir del esperado). */
    @Column(name = "monto_recibido", precision = 12, scale = 2)
    private BigDecimal montoRecibido;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "CREADA";

    @Column(name = "nombre_pagador", length = 200)
    private String nombrePagador;

    @Column(name = "nombre_cliente", length = 200)
    private String nombreCliente;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "CREADA";
        }
    }
}
