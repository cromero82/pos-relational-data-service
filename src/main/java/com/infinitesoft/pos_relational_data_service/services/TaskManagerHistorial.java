package com.infinitesoft.pos_relational_data_service.services;

import com.infinitesoft.pos_relational_data_service.entities.FlujoDinero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TaskManagerHistorial {

    private static final Logger log = LoggerFactory.getLogger(TaskManagerHistorial.class);

    @Autowired
    private FlujoDineroService flujoDineroService;

    @Value("${env.formats.date-default:yyyy-MM-dd}")
    private String dateFormat;

    @Scheduled(cron = "${env.task-managers.historial-total-by-date:0 59 23 * * ?}")
    public void generarFlujoHoy() {
        log.info("Iniciando tarea programada: generarFlujoHoy");
        try {
            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
            log.info("Generando flujo de dinero para la fecha: {}", fecha);

            FlujoDinero flujoGenerado = flujoDineroService.generateIngresoFromHistorial(fecha);

            if (flujoGenerado != null) {
                log.info("Se ha creado un nuevo registro en FlujoDinero con ID: {} y un total de: {}", flujoGenerado.getId(), flujoGenerado.getTotal());
            } else {
                log.warn("No se pudo generar el flujo de dinero para la fecha: {}. El servicio no retornó ningún objeto.", fecha);
            }
        } catch (Exception e) {
            log.error("Error durante la ejecución de la tarea programada generarFlujoHoy", e);
        }
        log.info("Finalizada tarea programada: generarFlujoHoy");
    }
}
