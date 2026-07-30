package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.entities.enums.DocumentoVentaEstado;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documento_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DocumentoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String consecutivo;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "historial_recibo_id", nullable = false, unique = true)
    private Long historialReciboId;

    @Column(name = "fecha_hecho", nullable = false)
    private LocalDateTime fechaHecho;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "sesion_id")
    private Long sesionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentoVentaEstado estado;

    @Column(name = "nota_ajuste_anulacion_id")
    private Long notaAjusteAnulacionId;

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
        if (estado == null) {
            estado = DocumentoVentaEstado.VIGENTE;
        }
        if (anio == null && fechaHecho != null) {
            anio = fechaHecho.getYear();
        }
    }
}
