package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "recibo_metodo_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboMetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recibo_id", nullable = false)
    private Long reciboId;

    @Column(name = "metodo_pago_id", nullable = false)
    private Long metodoPagoId;
}
