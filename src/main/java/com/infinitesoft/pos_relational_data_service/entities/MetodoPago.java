package com.infinitesoft.pos_relational_data_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metodo_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String estado;

    @Column(name = "file", length = 300)
    private String file;

    @Column(name = "sigla", length = 10)
    private String sigla;

    @Column(name = "color", length = 20)
    private String color;
}
