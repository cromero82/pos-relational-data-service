package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String documento;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @PrePersist
    @PreUpdate
    protected void onPrePersistUpdate() {
        StringUtils.convertStringsToUpperCase(this);
    }
}
