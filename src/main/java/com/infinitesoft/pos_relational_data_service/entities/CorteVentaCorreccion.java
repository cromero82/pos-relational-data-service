package com.infinitesoft.pos_relational_data_service.entities;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Log de corrección de un corte de ventas: registra el evento de negocio "corte original -> N
 * cortes nuevos" (tipo=SPLIT) o "edición de un corte sin restructurar el ledger" (tipo=EDICION).
 * Nunca se borra nada sin rastro (DIAN); ver 68_corte_venta_split.sql.
 */
@Entity
@Table(name = "corte_venta_correccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorteVentaCorreccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corte_original_id", nullable = false)
    private Long corteOriginalId;

    /** SPLIT | EDICION */
    @Column(nullable = false, length = 20)
    private String tipo;

    /** creado (único valor soportado por ahora). */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "creado";

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "usuario_id", nullable = false, length = 36)
    private String usuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "total_ventas_original", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalVentasOriginal;

    @Column(name = "rango_ini_original", nullable = false)
    private LocalDateTime rangoIniOriginal;

    @Column(name = "rango_fin_original", nullable = false)
    private LocalDateTime rangoFinOriginal;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = com.infinitesoft.pos_relational_data_service.util.DateUtils.obtenerFechaSistema();
        }
    }
}
