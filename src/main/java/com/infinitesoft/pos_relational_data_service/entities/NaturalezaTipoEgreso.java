package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "naturaleza_tipo_egreso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NaturalezaTipoEgreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @PrePersist
    @PreUpdate
    protected void normalize() {
        if (codigo != null) {
            codigo = codigo.trim().toUpperCase();
        }
        if (nombre != null) {
            nombre = nombre.trim().toUpperCase();
        }
        if (descripcion != null) {
            descripcion = descripcion.trim().toUpperCase();
        }
        if (activo == null) {
            activo = true;
        }
    }
}
