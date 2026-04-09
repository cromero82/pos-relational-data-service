package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CierreUsuarioService {

    private final EstadisticaFinancieraService estadisticaFinancieraService;

    @Async
    public void ejecutarCierre() {
        log.info("[CierreUsuario] Iniciando proceso de cierre para el usuario de manera ASÍNCRONA.");
        
        // 1. Rutina de estadísticas
        log.info("[CierreUsuario] Iniciando rutina de estadísticas financieras.");
        ejecutarRutinaEstadisticas();
        
        log.info("[CierreUsuario] Proceso de cierre finalizado.");
    }

    private void ejecutarRutinaEstadisticas() {
        LocalDate hoy = DateUtils.obtenerFechaSistema().toLocalDate();
        
        // Días
        log.info("[CierreUsuario] Procesando estadísticas por DÍA.");
        LocalDate diaIter = hoy.minusDays(1);
        while (true) {
            String valorTiempo = diaIter.format(DateTimeFormatter.ISO_LOCAL_DATE);
            if (estadisticaFinancieraService.existeEstadistica(valorTiempo)) {
                log.info("[CierreUsuario] Estadística DÍA {} ya existe. Finalizando búsqueda de días.", valorTiempo);
                break;
            }
            if (!estadisticaFinancieraService.tieneDatos(valorTiempo)) {
                log.info("[CierreUsuario] No hay datos (ventas/egresos) para DÍA {}. Finalizando búsqueda de días.", valorTiempo);
                break;
            }
            log.info("[CierreUsuario] Creando estadística DÍA {}.", valorTiempo);
            estadisticaFinancieraService.crearEstadisticaAsync(valorTiempo);
            diaIter = diaIter.minusDays(1);
        }

        // Meses
        log.info("[CierreUsuario] Procesando estadísticas por MES.");
        YearMonth mesActual = YearMonth.from(hoy);
        YearMonth mesIter = mesActual.minusMonths(1);
        while (true) {
            String valorTiempo = mesIter.toString(); // Formato YYYY-MM
            if (estadisticaFinancieraService.existeEstadistica(valorTiempo)) {
                log.info("[CierreUsuario] Estadística MES {} ya existe. Finalizando búsqueda de meses.", valorTiempo);
                break;
            }
            if (!estadisticaFinancieraService.tieneDatos(valorTiempo)) {
                log.info("[CierreUsuario] No hay datos (ventas/egresos) para MES {}. Finalizando búsqueda de meses.", valorTiempo);
                break;
            }
            log.info("[CierreUsuario] Creando estadística MES {}.", valorTiempo);
            estadisticaFinancieraService.crearEstadisticaAsync(valorTiempo);
            mesIter = mesIter.minusMonths(1);
        }

        // Años
        log.info("[CierreUsuario] Procesando estadísticas por AÑO.");
        int anioIter = hoy.getYear() - 1;
        while (true) {
            String valorTiempo = String.valueOf(anioIter);
            if (estadisticaFinancieraService.existeEstadistica(valorTiempo)) {
                log.info("[CierreUsuario] Estadística AÑO {} ya existe. Finalizando búsqueda de años.", valorTiempo);
                break;
            }
            if (!estadisticaFinancieraService.tieneDatos(valorTiempo)) {
                log.info("[CierreUsuario] No hay datos (ventas/egresos) para AÑO {}. Finalizando búsqueda de años.", valorTiempo);
                break;
            }
            log.info("[CierreUsuario] Creando estadística AÑO {}.", valorTiempo);
            estadisticaFinancieraService.crearEstadisticaAsync(valorTiempo);
            anioIter--;
        }
    }
}
