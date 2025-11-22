package com.infinitesoft.pos_relational_data_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "edicion_recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdicionRecibo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recibo_id")
    private Long reciboId;

    @Column(name = "historial_recibo_id")
    private Long historialReciboId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado_id", nullable = false)
    private Long estadoId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "sesion_id")
    private Long sesionId;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
}
