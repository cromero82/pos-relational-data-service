package com.infinitesoft.pos_relational_data_service.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "motivo_movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MotivoMovimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String categoria = "AJUSTE";

    @Column(nullable = false)
    @Builder.Default
    private Boolean sistema = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    /** TRASLADO_OF | REGISTRAR_DOCUMENTO | AJUSTE_CIERRE | REVISAR */
    @Column(name = "accion_esperada", length = 40)
    private String accionEsperada;
}
