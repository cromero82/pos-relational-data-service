package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaEgreso;
import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import lombok.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Snapshot del primer origen (compatibilidad / listados).
     * Fuente de verdad: {@link #origenes}.
     */
    @Column(name = "origen_fondos_id")
    private Integer origenFondosId;

    /**
     * Orígenes desde los que se paga el egreso (1:N). La suma de valores = {@link #valor}.
     */
    @OneToMany(mappedBy = "egreso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("orden ASC, id ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnoreProperties("egreso")
    private List<EgresoOrigenFondos> origenes = new ArrayList<>();

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

    /**
     * Líneas de origen a usar en ledger y validación.
     * Si el request solo trae {@code origenFondosId} (compat), se sintetiza una línea con el valor total.
     */
    public List<EgresoOrigenFondos> origenesEfectivos() {
        if (origenes != null && !origenes.isEmpty()) {
            return origenes.stream()
                    .sorted(Comparator
                            .comparing((EgresoOrigenFondos o) -> o.getOrden() != null ? o.getOrden() : 0)
                            .thenComparing(o -> o.getId() != null ? o.getId() : 0L))
                    .collect(Collectors.toList());
        }
        if (origenFondosId != null && valor != null) {
            return List.of(EgresoOrigenFondos.builder()
                    .egreso(this)
                    .origenFondosId(origenFondosId)
                    .metodoPagoId(metodoPagoId)
                    .valor(valor)
                    .orden(0)
                    .build());
        }
        return List.of();
    }
}
