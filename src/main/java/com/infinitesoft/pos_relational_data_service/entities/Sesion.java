package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sesion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cookie")
    private String cookie;

    @Column(name = "ultimo_ticket_id")
    private Long ultimoTicketId;
}
