package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrada_inventario_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EntradaInventarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_id", nullable = false)
    @JsonIgnore
    private EntradaInventario entrada;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "grupoEspejo"})
    private Product producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_compra_registrado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCompraRegistrado;

    @Column(name = "precio_compra_anterior", precision = 10, scale = 2)
    private BigDecimal precioCompraAnterior;

    @Column(name = "precio_venta_actual", precision = 10, scale = 2)
    private BigDecimal precioVentaActual;

    @Column(name = "precio_venta_nuevo", precision = 10, scale = 2)
    private BigDecimal precioVentaNuevo;

    @Column(name = "porcentaje_ganancia_calc")
    private Short porcentajeGananciaCalc;

    @Column(name = "porcentaje_variacion_compra", precision = 8, scale = 2)
    private BigDecimal porcentajeVariacionCompra;

    @Column(name = "alerta_precio_subio", nullable = false)
    @Builder.Default
    private Boolean alertaPrecioSubio = false;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
        if (alertaPrecioSubio == null) {
            alertaPrecioSubio = false;
        }
    }
}
