package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @Column(name = "fecha_actualizacion_precio", updatable = false)
    private LocalDateTime fechaUltimaActualizacionPrecio;

    // 1 = active, 0 = deleted
    @Column(name = "activate")
    @Builder.Default
    private Integer activate = 1;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}