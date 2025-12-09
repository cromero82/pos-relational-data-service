package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "producto", schema = "dbo")
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
    @Builder.Default
    private Double precioCompra = 0.0;

//    @Column(name = "foto")
//    private String foto;

    // 1 = active, 0 = deleted
    @Column(name = "activate")
    @Builder.Default
    private Integer activate = 1;
}
