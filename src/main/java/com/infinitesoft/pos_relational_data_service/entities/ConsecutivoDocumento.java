package com.infinitesoft.pos_relational_data_service.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "consecutivo_documento", uniqueConstraints = {
        @UniqueConstraint(name = "uq_consecutivo_tipo_anio", columnNames = {"tipo", "anio"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsecutivoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero;

    @Column(nullable = false, length = 10)
    private String prefijo;
}
