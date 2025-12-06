package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ticket_recibo",
       uniqueConstraints = {@UniqueConstraint(name = "uk_ticket_recibo", columnNames = {"ticket_id", "recibo_id"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketRecibo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", insertable = false, updatable = false)
    @JsonIgnore
    private Ticket ticket;

    @Column(name = "recibo_id", nullable = false)
    private Long reciboId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_id", insertable = false, updatable = false)
    @JsonIgnore
    private Recibo recibo;
}
