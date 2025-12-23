package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100)
    private String nombre;

    @Column(length = 25)
    private String sigla;
}
