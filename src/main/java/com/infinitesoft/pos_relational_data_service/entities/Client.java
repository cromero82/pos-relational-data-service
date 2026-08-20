package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;   // DB default 'anonimo'
    private String telefono;
    private String documento;
    /** Contacto opcional; se guarda en minúsculas (no pasa por UPPER global). */
    private String correo;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        String correoKeep = this.correo;
        StringUtils.convertStringsToUpperCase(this);
        if (correoKeep != null && !correoKeep.isBlank()) {
            this.correo = correoKeep.trim().toLowerCase();
        } else {
            this.correo = null;
        }
    }
}
