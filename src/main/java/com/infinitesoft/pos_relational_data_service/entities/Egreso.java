package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "origen_fondos_id")
    private Integer origenFondosId;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        StringUtils.convertStringsToUpperCase(this);
    }
}
