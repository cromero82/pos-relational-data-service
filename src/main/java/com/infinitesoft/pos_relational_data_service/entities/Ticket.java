package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import javax.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Keep only the sessionId in JSON as a reference
    @Column(name = "sesion_id")
    private Long sessionId;

    // Optional relation for internal use; hidden from JSON and not used to write FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_id", insertable = false, updatable = false)
    @JsonIgnore
    private Sesion sesion;

    private String nombre;
    
    @Column(name = "orden", nullable = false)
    @Builder.Default
    private Long orden = 1L;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
    }
}
