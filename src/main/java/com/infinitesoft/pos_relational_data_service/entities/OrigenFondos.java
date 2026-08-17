package com.infinitesoft.pos_relational_data_service.entities;

import com.infinitesoft.pos_relational_data_service.entities.enums.NaturalezaOrigenFondos;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "origen_fondos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrigenFondos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "tipo_origen_fondos_id", nullable = false)
    private Integer tipoOrigenFondosId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_origen_fondos_id", insertable = false, updatable = false)
    private TipoOrigenFondos tipoOrigenFondos;

    @Column(name = "proveedor_id")
    private Long proveedorId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "parent_origen_fondos_id")
    private Integer parentOrigenFondosId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_origen_fondos_id", insertable = false, updatable = false)
    private OrigenFondos parentOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private NaturalezaOrigenFondos naturaleza = NaturalezaOrigenFondos.ELECTRONICA;

    @Column(name = "visible_en_egreso", nullable = false)
    @Builder.Default
    private Boolean visibleEnEgreso = true;

    @Column(name = "requiere_conciliacion", nullable = false)
    @Builder.Default
    private Boolean requiereConciliacion = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    /**
     * ACTIVO | ARCHIVADO. Archivado ⇒ activo=false (oculto en árbol/listas).
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(length = 20)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
    private LocalDateTime fechaCreacion;
}
