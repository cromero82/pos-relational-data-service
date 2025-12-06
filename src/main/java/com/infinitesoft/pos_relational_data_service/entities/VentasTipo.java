package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ventas_tipo",
       uniqueConstraints = {@UniqueConstraint(name = "uk_ventas_tipo_metodo_fecha", columnNames = {"metodo_pago_id", "fecha"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentasTipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
}
