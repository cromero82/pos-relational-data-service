package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaEgreso;
import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "egreso", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Egreso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Proveedor proveedor;

    /**
     * Beneficiario persona (PERSONAL / DIVIDENDOS). Excluyente con proveedor.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Persona persona;

    /**
     * Snapshot del tipo al momento del egreso (independiente de cambios futuros del proveedor).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_egreso_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TipoEgreso tipoEgreso;

    /**
     * Propósito del pago. Ver {@link NaturalezaEgreso}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "naturaleza", length = 40)
    private NaturalezaEgreso naturaleza;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "origen_fondos_id")
    private Integer origenFondosId;

    /**
     * Movimiento OF (p.ej. entrada en Para ordenar por email) que se formaliza.
     * La salida de caja del egreso sale de ese OF; no resta de nuevo el banco.
     */
    @Column(name = "from_movimiento_origen_fondos_id")
    private Long fromMovimientoOrigenFondosId;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        StringUtils.convertStringsToUpperCase(this);
    }
}
