package com.infinitesoft.pos_relational_data_service.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras", unique = true)
    private String barcode;

    private String nombre;
    private Double precio;

    @Column(name = "precio_compra")
    private Double precioVenta = 0.0;
    private String foto;

}