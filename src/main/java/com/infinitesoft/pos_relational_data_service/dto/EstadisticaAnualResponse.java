package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.TipoResultadoFin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticaAnualResponse {
    private Integer id;
    private LocalDateTime fechaCreacion;
    private BigDecimal totalEgresos;
    private BigDecimal totalVentas;
    private BigDecimal totalCobranzas;
    private BigDecimal utilidad;
    private BigDecimal porcentajeUtilidad;
    private Date anio;
    private String valorTiempo;
    private TipoResultadoFin tipoResultadoFin;
}
