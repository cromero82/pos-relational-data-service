package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nota_ajuste_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class NotaAjusteDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, length = 30)
    private String consecutivo;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "documento_venta_origen_id", nullable = false)
    private Long documentoVentaOrigenId;

    @Column(name = "motivo_operacion_id")
    private Long motivoOperacionId;

    @Column(name = "motivo_texto", columnDefinition = "TEXT")
    private String motivoTexto;

    @Column(name = "total_ajuste", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAjuste;

    @Column(name = "fecha_hecho", nullable = false)
    private LocalDateTime fechaHecho;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "historial_recibo_id")
    private Long historialReciboId;

    @Column(name = "operacion_restauracion", nullable = false)
    private Boolean operacionRestauracion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaHecho == null) {
            fechaHecho = LocalDateTime.now();
        }
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (operacionRestauracion == null) {
            operacionRestauracion = false;
        }
        if (anio == null && fechaHecho != null) {
            anio = fechaHecho.getYear();
        }
    }
}
