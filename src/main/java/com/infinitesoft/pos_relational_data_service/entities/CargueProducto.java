package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cargue_productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargueProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "total_migrados")
    private Integer totalMigrados;

    @Column(name = "total_conflictos")
    private Integer totalConflictos;

    @Column(name = "total_conflictos_resultos")
    private Integer totalConflictosResultos;

    @Column(name = "mensajes_error", columnDefinition = "TEXT")
    private String mensajesError;
}
