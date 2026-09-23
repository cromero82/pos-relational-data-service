package com.infinitesoft.pos_relational_data_service.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "funcionalidad_rol")
@IdClass(FuncionalidadRolId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuncionalidadRol {
    @Id
    @Column(name = "funcionalidad_id")
    private Integer funcionalidadId;

    @Id
    @Column(name = "rol_sigla", length = 50)
    private String rolSigla;

    @Column(name = "puede_leer", nullable = false)
    @Builder.Default
    private Boolean puedeLeer = true;

    @Column(name = "puede_escribir", nullable = false)
    @Builder.Default
    private Boolean puedeEscribir = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionalidad_id", insertable = false, updatable = false)
    private FuncionalidadPos funcionalidad;
}
