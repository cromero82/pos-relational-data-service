package com.infinitesoft.pos_relational_data_service.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "funcionalidad_pos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuncionalidadPos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String seccion;

    @Column(name = "ruta_front", length = 200)
    private String rutaFront;

    @Column(name = "etiqueta_menu", length = 100)
    private String etiquetaMenu;

    @Column(name = "badge_ui", length = 40)
    private String badgeUi;

    @Column(name = "requiere_disclaimer", nullable = false)
    @Builder.Default
    private Boolean requiereDisclaimer = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
