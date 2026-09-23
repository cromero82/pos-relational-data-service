package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ventas_tipo")
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

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "total_sistema", precision = 10, scale = 2)
    private BigDecimal totalSistema;

    @Column(name = "total_ventas_sistema", precision = 12, scale = 2)
    private BigDecimal totalVentasSistema;

    @Column(name = "total_egresos_sistema", precision = 12, scale = 2)
    private BigDecimal totalEgresosSistema;

    @Column(name = "desfase", precision = 12, scale = 2)
    private BigDecimal desfase;

    @Column(name = "motivo_desfase_id")
    private Integer motivoDesfaseId;

    @Column(name = "corte_venta_id")
    private Long corteVentaId;
}
