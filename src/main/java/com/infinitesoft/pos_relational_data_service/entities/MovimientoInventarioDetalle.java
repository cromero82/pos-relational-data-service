package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "movimiento_inventario_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventarioDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movimiento_id", nullable = false)
    private Long movimientoId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "cantidad_sistema", precision = 12, scale = 3)
    private BigDecimal cantidadSistema;

    @Column(name = "cantidad_contada", precision = 12, scale = 3)
    private BigDecimal cantidadContada;

    @Column(name = "motivo_linea")
    private String motivoLinea;

    @Column(name = "direccion_linea", nullable = false, length = 10)
    private String direccionLinea;
}
