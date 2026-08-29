package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto_presentacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductoPresentacion {

    public static final String CODIGO_PAQUETE = "PAQUETE";
    public static final String CODIGO_UNIDAD = "UNIDAD";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", insertable = false, updatable = false)
    @JsonIgnore
    private Product producto;

    @Column(nullable = false, length = 32)
    private String codigo;

    @Column(name = "nombre_mostrar", nullable = false, length = 120)
    private String nombreMostrar;

    @Column(name = "factor_a_base", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal factorABase = BigDecimal.ONE;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @Column(name = "codigo_barras_alt", length = 64)
    private String codigoBarrasAlt;

    @Column(name = "es_default_venta", nullable = false)
    @Builder.Default
    private Boolean esDefaultVenta = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = DateUtils.obtenerFechaSistema();
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
        fechaActualizacion = now;
        if (factorABase == null || factorABase.compareTo(BigDecimal.ZERO) <= 0) {
            factorABase = BigDecimal.ONE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = DateUtils.obtenerFechaSistema();
    }
}
