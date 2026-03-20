package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_egreso")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TipoEgreso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        StringUtils.convertStringsToUpperCase(this);
    }
}
