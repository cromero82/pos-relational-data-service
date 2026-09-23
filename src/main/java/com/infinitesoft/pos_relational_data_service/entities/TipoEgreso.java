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

    /**
     * Naturaleza sugerida al usar este tipo en un egreso (catálogo 1→N).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "naturaleza_tipo_egreso_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private NaturalezaTipoEgreso naturaleza;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        StringUtils.convertStringsToUpperCase(this);
    }
}
