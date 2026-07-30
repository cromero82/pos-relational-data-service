package com.infinitesoft.pos_relational_data_service.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "tipo_origen_fondos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoOrigenFondos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
