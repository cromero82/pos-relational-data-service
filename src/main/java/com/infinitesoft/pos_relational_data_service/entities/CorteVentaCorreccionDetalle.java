package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detalle de una corrección: qué corte nuevo nació de ella, en qué rango y en qué orden
 * (para reconstruir la secuencia cronológica de particiones de un SPLIT).
 */
@Entity
@Table(name = "corte_venta_correccion_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteVentaCorreccionDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correccion_id", nullable = false)
    private Long correccionId;

    @Column(name = "corte_nuevo_id", nullable = false)
    private Long corteNuevoId;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDateTime fechaHasta;

    @Column(nullable = false)
    private Integer orden;
}
