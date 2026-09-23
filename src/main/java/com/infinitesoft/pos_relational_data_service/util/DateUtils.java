package com.infinitesoft.pos_relational_data_service.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    /**
     * Obtiene la fecha y hora actual del sistema sin milisegundos.
     * @return LocalDateTime truncado al segundo.
     */
    public static LocalDateTime obtenerFechaSistema() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
