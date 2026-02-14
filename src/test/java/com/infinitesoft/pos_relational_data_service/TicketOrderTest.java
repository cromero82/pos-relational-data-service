package com.infinitesoft.pos_relational_data_service;

import com.infinitesoft.pos_relational_data_service.entities.Sesion;
import com.infinitesoft.pos_relational_data_service.entities.Ticket;
import com.infinitesoft.pos_relational_data_service.repositories.SesionRepository;
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository;
import com.infinitesoft.pos_relational_data_service.services.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class TicketOrderTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Test
    void testFindBySessionIdOrderByOrdenAsc() {
        // 1. Crear una sesión de prueba
        Sesion sesion = Sesion.builder()
                .userId(UUID.randomUUID())
                .esActivo(true)
                .build();
        sesion = sesionRepository.save(sesion);
        Long sessionId = sesion.getId();

        // 2. Crear tickets con diferentes valores de 'orden' desordenados en inserción
        Ticket t1 = Ticket.builder()
                .sessionId(sessionId)
                .nombre("Ticket 2")
                .orden(2L)
                .build();
        
        Ticket t2 = Ticket.builder()
                .sessionId(sessionId)
                .nombre("Ticket 1")
                .orden(1L)
                .build();
        
        Ticket t3 = Ticket.builder()
                .sessionId(sessionId)
                .nombre("Ticket 3")
                .orden(3L)
                .build();

        ticketRepository.save(t1);
        ticketRepository.save(t2);
        ticketRepository.save(t3);

        // 3. Recuperar por sessionId
        List<Ticket> results = ticketService.findBySessionId(sessionId);

        // 4. Verificar el orden
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getOrden()).isEqualTo(1L);
        assertThat(results.get(1).getOrden()).isEqualTo(2L);
        assertThat(results.get(2).getOrden()).isEqualTo(3L);
        assertThat(results.get(0).getNombre()).isEqualTo("Ticket 1");
    }

    @Test
    void testAutomaticOrdenCalculation() {
        // 1. Crear una sesión de prueba
        Sesion sesion = Sesion.builder()
                .userId(UUID.randomUUID())
                .esActivo(true)
                .build();
        sesion = sesionRepository.save(sesion);
        Long sessionId = sesion.getId();

        // 2. Crear el primer ticket sin especificar orden
        Ticket t1 = Ticket.builder()
                .sessionId(sessionId)
                .nombre("Ticket A")
                .build();
        t1 = ticketService.create(t1);
        assertThat(t1.getOrden()).isEqualTo(1L);

        // 3. Crear el segundo ticket sin especificar orden
        Ticket t2 = Ticket.builder()
                .sessionId(sessionId)
                .nombre("Ticket B")
                .build();
        t2 = ticketService.create(t2);
        assertThat(t2.getOrden()).isEqualTo(2L);

        // 4. Crear el tercer ticket con una orden específica manual (saltándose una)
        Ticket t3 = Ticket.builder()
                .sessionId(sessionId)
                .nombre("Ticket C")
                .orden(10L)
                .build();
        // Nota: Actualmente create sobreescribe el orden si sessionId no es null. 
        // Vamos a verificar el comportamiento implementado.
        t3 = ticketService.create(t3);
        assertThat(t3.getOrden()).isEqualTo(3L); // Según mi implementación sobreescribe siempre si hay sessionId

        // 5. Verificar que findBySessionId los trae en orden
        List<Ticket> results = ticketService.findBySessionId(sessionId);
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getOrden()).isEqualTo(1L);
        assertThat(results.get(1).getOrden()).isEqualTo(2L);
        assertThat(results.get(2).getOrden()).isEqualTo(3L);
    }

    @Test
    void testBulkUpdateTickets() {
        // 1. Crear sesión y tickets
        Sesion sesion = Sesion.builder()
                .userId(UUID.randomUUID())
                .esActivo(true)
                .build();
        sesion = sesionRepository.save(sesion);
        Long sessionId = sesion.getId();

        Ticket t1 = ticketService.create(Ticket.builder().sessionId(sessionId).nombre("T1").build());
        Ticket t2 = ticketService.create(Ticket.builder().sessionId(sessionId).nombre("T2").build());
        
        assertThat(t1.getOrden()).isEqualTo(1L);
        assertThat(t2.getOrden()).isEqualTo(2L);

        // 2. Preparar actualización masiva (invertir orden)
        t1.setOrden(2L);
        t2.setOrden(1L);
        
        List<Ticket> toUpdate = List.of(t1, t2);
        List<Ticket> updated = ticketService.updateAll(toUpdate);
        
        assertThat(updated).hasSize(2);
        
        // 3. Verificar en base de datos el nuevo orden
        List<Ticket> results = ticketService.findBySessionId(sessionId);
        assertThat(results.get(0).getNombre()).isEqualTo("T2");
        assertThat(results.get(0).getOrden()).isEqualTo(1L);
        assertThat(results.get(1).getNombre()).isEqualTo("T1");
        assertThat(results.get(1).getOrden()).isEqualTo(2L);
    }
}
