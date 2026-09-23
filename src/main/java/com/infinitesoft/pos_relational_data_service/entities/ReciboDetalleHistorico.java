package com.infinitesoft.pos_relational_data_service.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recibo_detalle_historico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboDetalleHistorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recibo_detalle_id", nullable = false)
    private Long reciboDetalleId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "usuario_id", nullable = false, length = 36)
    private String usuarioId;

    @Column(nullable = false, length = 100)
    private String accion;
}
