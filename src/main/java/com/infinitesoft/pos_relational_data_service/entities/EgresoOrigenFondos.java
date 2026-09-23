package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "egreso_origen_fondos", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "egreso")
@ToString(exclude = "egreso")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EgresoOrigenFondos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "egreso_id", nullable = false)
    @JsonIgnore
    private Egreso egreso;

    @Column(name = "origen_fondos_id", nullable = false)
    private Integer origenFondosId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;
}
