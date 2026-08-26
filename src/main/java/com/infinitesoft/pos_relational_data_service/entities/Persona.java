package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "persona")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String documento;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * Dueño/propietario del establecimiento. Habilita egreso desde OF Cuenta del dueño.
     */
    @Column(name = "es_dueno_propietario", nullable = false)
    @Builder.Default
    private Boolean esDuenoPropietario = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime fechaCreacion;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        StringUtils.convertStringsToUpperCase(this);
        if (activo == null) {
            activo = true;
        }
        if (esDuenoPropietario == null) {
            esDuenoPropietario = false;
        }
    }
}
