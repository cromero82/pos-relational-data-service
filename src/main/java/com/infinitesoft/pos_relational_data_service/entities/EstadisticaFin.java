package com.infinitesoft.pos_relational_data_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.infinitesoft.pos_relational_data_service.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "estadistica_fin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EstadisticaFin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "total_egresos", nullable = true, precision = 15, scale = 2)
    private BigDecimal totalEgresos;

    @Column(name = "total_ventas", nullable = true, precision = 15, scale = 2)
    private BigDecimal totalVentas;

    /** Abonos CxC ({@code ENTRADA_COBRANZA}) del periodo. */
    @Column(name = "total_cobranzas", nullable = true, precision = 15, scale = 2)
    private BigDecimal totalCobranzas;

    @Column(nullable = true, precision = 15, scale = 2)
    private BigDecimal utilidad;

    @Column(name = "porcentaje_utilidad", nullable = true, precision = 6, scale = 2)
    private BigDecimal porcentajeUtilidad;

    @Column(name = "formato_tiempo", nullable = false, length = 10)
    private String formatoTiempo;

    @Column(name = "valor_tiempo", nullable = false, length = 20)
    private String valorTiempo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_resultado_fin_id")
    private TipoResultadoFin tipoResultadoFin;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = DateUtils.obtenerFechaSistema();
        }
    }
}
