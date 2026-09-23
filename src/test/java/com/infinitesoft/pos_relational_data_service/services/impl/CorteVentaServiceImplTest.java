package com.infinitesoft.pos_relational_data_service.services.impl;

import com.infinitesoft.pos_relational_data_service.entities.CorteVenta;
import com.infinitesoft.pos_relational_data_service.repositories.CorteVentaDetalleRepository;
import com.infinitesoft.pos_relational_data_service.repositories.CorteVentaRepository;
import com.infinitesoft.pos_relational_data_service.repositories.EgresoRepository;
import com.infinitesoft.pos_relational_data_service.repositories.HistorialReciboRepository;
import com.infinitesoft.pos_relational_data_service.services.MovimientoOrigenFondosService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorteVentaServiceImplTest {

    @Mock
    private CorteVentaRepository repository;
    @Mock
    private CorteVentaDetalleRepository detalleRepository;
    @Mock
    private HistorialReciboRepository historialReciboRepository;
    @Mock
    private EgresoRepository egresoRepository;
    @Mock
    private MovimientoOrigenFondosService movimientoOrigenFondosService;

    @InjectMocks
    private CorteVentaServiceImpl service;

    @Test
    void eliminarUltimoCorteHaceSoftDeleteYRevierteAjustes() {
        CorteVenta corte = CorteVenta.builder().id(10L).estado("creada").build();
        when(repository.findById(10L)).thenReturn(Optional.of(corte));
        when(repository.findFirstByEstadoNotOrderByIdDesc("eliminado"))
                .thenReturn(Optional.of(corte));

        assertTrue(service.delete(10L));

        verify(movimientoOrigenFondosService).revertirAjustesCierre(10L);
        verify(repository).save(corte);
        assertEquals("eliminado", corte.getEstado());
    }

    @Test
    void eliminarCorteQueNoEsUltimoEsRechazado() {
        CorteVenta corte = CorteVenta.builder().id(9L).estado("revisada").build();
        CorteVenta ultimo = CorteVenta.builder().id(10L).estado("creada").build();
        when(repository.findById(9L)).thenReturn(Optional.of(corte));
        when(repository.findFirstByEstadoNotOrderByIdDesc("eliminado"))
                .thenReturn(Optional.of(ultimo));

        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> service.delete(9L));

        assertTrue(error.getMessage().contains("último corte"));
        verify(movimientoOrigenFondosService, never()).revertirAjustesCierre(anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    void eliminarCorteYaEliminadoEsIdempotente() {
        CorteVenta corte = CorteVenta.builder().id(10L).estado("eliminado").build();
        when(repository.findById(10L)).thenReturn(Optional.of(corte));

        assertTrue(service.delete(10L));

        verifyNoInteractions(movimientoOrigenFondosService);
        verify(repository, never()).save(any());
    }
}
