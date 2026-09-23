package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta al marcar recibo PAGADO (historial + documento de venta).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboPagoResponseDto {
    private boolean pagado;
    private Long historialReciboId;
    private Long documentoVentaId;
    private String documentoVentaConsecutivo;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;
    private Long metodoPagoId;
    private Long clienteId;
    private Long sesionId;
    /** Desglose de cobro persistido (1–N). */
    private java.util.List<ReciboPagoLineaDto> pagos;
}
