package com.infinitesoft.pos_relational_data_service.repositories;

import com.infinitesoft.pos_relational_data_service.dto.ReciboDetalleDto;
import com.infinitesoft.pos_relational_data_service.entities.Client;
import com.infinitesoft.pos_relational_data_service.entities.Product;
import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import com.infinitesoft.pos_relational_data_service.entities.ReciboDetalle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ReciboDetalleRepositoryOrderTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReciboDetalleRepository repository;

    private Long reciboId;

    @BeforeEach
    void setUp() {
        Client client = Client.builder()
                .nombre("Test Client")
                .documento("123456")
                .build();
        client = entityManager.persistAndFlush(client);

        Recibo recibo = Recibo.builder()
                .clienteId(client.getId())
                .estadoId(1L)
                .total(BigDecimal.ZERO)
                .montoRecibido(BigDecimal.ZERO)
                .build();
        recibo = entityManager.persistAndFlush(recibo);
        reciboId = recibo.getId();

        Product p1 = Product.builder().nombre("P1").precio(10.0).activate(1).build();
        Product p2 = Product.builder().nombre("P2").precio(20.0).activate(1).build();
        p1 = entityManager.persistAndFlush(p1);
        p2 = entityManager.persistAndFlush(p2);

        // Insertar en orden no secuencial de ID si fuera posible, pero IDENTITY lo hará secuencial.
        // Lo importante es que la consulta garantice el orden.
        ReciboDetalle d1 = ReciboDetalle.builder().reciboId(reciboId).productoId(p1.getId()).cantidad(1).subtotal(new BigDecimal("10.0")).build();
        ReciboDetalle d2 = ReciboDetalle.builder().reciboId(reciboId).productoId(p2.getId()).cantidad(2).subtotal(new BigDecimal("20.0")).build();
        ReciboDetalle d3 = ReciboDetalle.builder().reciboId(reciboId).productoId(p1.getId()).cantidad(3).subtotal(new BigDecimal("30.0")).build();

        entityManager.persist(d1);
        entityManager.persist(d2);
        entityManager.persist(d3);
        entityManager.flush();
    }

    @Test
    void findByReciboIdOrderByIdAsc_ShouldReturnOrderedList() {
        List<ReciboDetalle> detalles = repository.findByReciboIdOrderByIdAsc(reciboId);
        
        assertThat(detalles).hasSize(3);
        assertThat(detalles.get(0).getId()).isLessThan(detalles.get(1).getId());
        assertThat(detalles.get(1).getId()).isLessThan(detalles.get(2).getId());
    }

    @Test
    void findDtoByReciboId_ShouldReturnOrderedList() {
        List<ReciboDetalleDto> dtos = repository.findDtoByReciboId(reciboId);

        assertThat(dtos).hasSize(3);
        assertThat(dtos.get(0).getId()).isLessThan(dtos.get(1).getId());
        assertThat(dtos.get(1).getId()).isLessThan(dtos.get(2).getId());
    }
}
