package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_precio_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HistorialPrecioProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entrada_inventario_detalle_id", nullable = false)
    private Long entradaInventarioDetalleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrada_inventario_detalle_id", insertable = false, updatable = false)
    @JsonIgnore
    private EntradaInventarioDetalle entradaInventarioDetalle;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "grupoEspejo"})
    private Product producto;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "precio_compra_antes", precision = 10, scale = 2)
    private BigDecimal precioCompraAntes;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "precio_venta_antes", precision = 10, scale = 2)
    private BigDecimal precioVentaAntes;

    @Column(name = "porcentaje_ganancia")
    private Short porcentajeGanancia;

    @Column(name = "porcentaje_ganancia_antes")
    private Short porcentajeGananciaAntes;

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
    }
}
