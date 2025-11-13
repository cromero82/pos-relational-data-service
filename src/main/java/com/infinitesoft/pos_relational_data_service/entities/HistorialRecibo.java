package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado_id", nullable = false)
    private Long estadoId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
}
