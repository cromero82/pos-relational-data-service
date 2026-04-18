package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "grupo_espejo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GrupoEspejo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "fecha_creacion", updatable = false)
    private java.time.LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private java.time.LocalDateTime fechaActualizacion;

    @Column(name = "producto_referencia_id")
    private Long productoReferenciaId;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = DateUtils.obtenerFechaSistema();
        fechaActualizacion = DateUtils.obtenerFechaSistema();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = DateUtils.obtenerFechaSistema();
    }
}
