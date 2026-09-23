package com.infinitesoft.pos_relational_data_service.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "motivo_operacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotivoOperacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "aplica_a", nullable = false, length = 40)
    private String aplicaA;

    @Column(nullable = false)
    private Boolean activo;
}
