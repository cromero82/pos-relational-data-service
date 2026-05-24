package com.infinitesoft.pos_relational_data_service.entities;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "configuracion_app")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key", length = 50, nullable = false, unique = true)
    private String key;

    @Column(name = "value", length = 1000, nullable = false)
    private String value;
}
