package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "edicion_recibo_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdicionReciboDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "edicion_id", nullable = false)
    private Long edicionId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "presentacion_id")
    private Long presentacionId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "cantidad_base", precision = 14, scale = 4)
    private BigDecimal cantidadBase;

    @Column(name = "precio_unitario_snapshot", precision = 12, scale = 2)
    private BigDecimal precioUnitarioSnapshot;

    @Column(name = "factor_snapshot", precision = 12, scale = 4)
    private BigDecimal factorSnapshot;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
