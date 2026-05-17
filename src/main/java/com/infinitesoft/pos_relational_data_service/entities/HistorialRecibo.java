package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "historial_recibo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialRecibo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    @JsonIgnore
    private Client cliente;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado_id", nullable = false)
    private Long estadoId;

    @Column(name = "metodo_pago_id")
    private Long metodoPagoId;

    /**
     * Lista de IDs de métodos de pago (relación 1-N vía tabla historial_recibo_metodo_pago).
     * Campo transient: no se persiste en esta tabla, se gestiona por el servicio.
     */
    @Transient
    private List<Long> metodoPagoIds;

    @JsonAlias("sessionId")
    @Column(name = "sesion_id")
    private Long sesionId;


    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "monto_recibido", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoRecibido;

    @PrePersist
    protected void onPrePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
    }
}
