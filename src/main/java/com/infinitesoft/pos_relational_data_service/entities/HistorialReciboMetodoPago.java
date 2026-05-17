package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "historial_recibo_metodo_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialReciboMetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "historial_recibo_id", nullable = false)
    private Long historialReciboId;

    @Column(name = "metodo_pago_id", nullable = false)
    private Long metodoPagoId;
}
