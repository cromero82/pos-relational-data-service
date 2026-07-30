package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_movimiento_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 10)
    private String direccion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
