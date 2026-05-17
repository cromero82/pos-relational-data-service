package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "edicion_recibo_metodo_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdicionReciboMetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "edicion_recibo_id", nullable = false)
    private Long edicionReciboId;

    @Column(name = "metodo_pago_id", nullable = false)
    private Long metodoPagoId;
}
