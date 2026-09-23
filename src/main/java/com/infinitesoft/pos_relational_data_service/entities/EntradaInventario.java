package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.entities.enums.EntradaInventarioEstado;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entrada_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EntradaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "egreso_id", nullable = false, unique = true)
    private Long egresoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "egreso_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Egreso egreso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EntradaInventarioEstado estado = EntradaInventarioEstado.BORRADOR;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "total_items")
    @Builder.Default
    private Integer totalItems = 0;

    @OneToMany(mappedBy = "entrada", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("entrada")
    private List<EntradaInventarioDetalle> detalles = new ArrayList<>();

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
        if (estado == null) {
            estado = EntradaInventarioEstado.BORRADOR;
        }
        if (totalItems == null) {
            totalItems = 0;
        }
    }
}
