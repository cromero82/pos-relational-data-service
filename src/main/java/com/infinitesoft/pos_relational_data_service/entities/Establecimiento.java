package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "establecimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 200)
    private String nombreComercial;

    @Column(length = 20)
    private String nit;

    @Column(name = "digito_verificacion", length = 2)
    private String digitoVerificacion;

    @Column(name = "regimen_tributario", nullable = false, length = 40)
    private String regimenTributario;

    @Column(name = "regimen_leyenda_impresion", nullable = false, length = 300)
    private String regimenLeyendaImpresion;

    @Column(length = 300)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(length = 120)
    private String email;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "manejo_estricto_cuentas", nullable = false)
    @Builder.Default
    private Boolean manejoEstrictoCuentas = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = java.time.LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = now;
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = now;
        }
        if (activo == null) {
            activo = Boolean.TRUE;
        }
        if (manejoEstrictoCuentas == null) {
            manejoEstrictoCuentas = Boolean.FALSE;
        }
        if (regimenTributario == null) {
            regimenTributario = "NO_RESPONSABLE_IVA";
        }
        if (regimenLeyendaImpresion == null) {
            regimenLeyendaImpresion = "Establecimiento NO RESPONSABLE DE IVA";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = java.time.LocalDateTime.now();
    }
}
