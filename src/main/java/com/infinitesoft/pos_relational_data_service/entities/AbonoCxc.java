package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "abono_cxc", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AbonoCxc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_por_cobrar_id", nullable = false)
    private Long cuentaPorCobrarId;

    @Column(name = "fecha_abono", nullable = false)
    private LocalDateTime fechaAbono;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "metodo_pago_id", nullable = false)
    private Long metodoPagoId;

    @Column(name = "origen_fondos_id")
    private Integer origenFondosId;

    @Column(name = "movimiento_origen_fondos_id")
    private Long movimientoOrigenFondosId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    /** Cliente que entrega el abono (puede diferir del deudor de la CxC). */
    @Column(name = "cliente_pagador_id")
    private Long clientePagadorId;

    /** Snapshot del nombre al registrar. */
    @Column(name = "cliente_pagador_nombre", length = 255)
    private String clientePagadorNombre;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onPrePersist() {
        LocalDateTime now = DateUtils.obtenerFechaSistema();
        if (fechaAbono == null) {
            fechaAbono = now;
        }
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
    }
}
