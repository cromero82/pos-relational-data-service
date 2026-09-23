package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras", unique = true)
    private String barcode;

    private String nombre;
    private Double precio;

    @Column(name = "precio_compra")
    private Double precioCompra = 0.0;

    @Column(name = "precio_unidad")
    private Double precioUnidad;

    @Column(name = "fecha_actualizacion_precio")
    private LocalDateTime fechaUltimaActualizacionPrecio;

    // 1 = active, 0 = deleted
    @Column(name = "activate")
    @Builder.Default
    private Integer activate = 1;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "total_ventas")
    @Builder.Default
    private Integer totalVentas = 0;

    @Column(name = "fecha_ultima_venta")
    private LocalDate fechaUltimaVenta;

    @Column(name = "porcentaje_ganancia")
    private Short porcentajeGanancia;

    @Column(name = "existencia")
    @Builder.Default
    private Integer existencia = 0;

    /** Código de la unidad base de inventario (p.ej. UNIDAD). Documental. */
    @Column(name = "unidad_base_codigo", length = 32)
    @Builder.Default
    private String unidadBaseCodigo = "UNIDAD";

    /** Presentaciones vendibles (cargadas bajo demanda / enrich). */
    @Transient
    private java.util.List<ProductoPresentacion> presentaciones;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
            name = "producto_espejo",
            joinColumns = @JoinColumn(name = "producto_id", insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "grupo_espejo_id", insertable = false, updatable = false)
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fechaCreacion", "fechaActualizacion", "productoReferenciaId"})
    private GrupoEspejo grupoEspejo;

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
        if (fechaUltimaActualizacionPrecio == null) {
            fechaUltimaActualizacionPrecio = DateUtils.obtenerFechaSistema();
        }
        StringUtils.convertStringsToUpperCase(this);
    }

    @PreUpdate
    protected void onPreUpdate() {
        StringUtils.convertStringsToUpperCase(this);
    }
}