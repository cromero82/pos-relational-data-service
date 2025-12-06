package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "flujo_dinero")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlujoDinero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Only date, no time
    @Column(name = "fecha", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    // Values like 1 or 2
    @Column(name = "tipo_id", nullable = false)
    private Integer tipoId;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "user_id")
    private Long userId;
}