package com.infinitesoft.pos_relational_data_service.dto;

import com.infinitesoft.pos_relational_data_service.entities.Recibo;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReciboUpdateResult {
    private Recibo recibo;
    private ReciboPagoResponseDto pagoResponse;

    public boolean isPagoCompletado() {
        return pagoResponse != null && pagoResponse.isPagado();
    }

    public static ReciboUpdateResult fromRecibo(Recibo recibo) {
        return ReciboUpdateResult.builder().recibo(recibo).build();
    }

    public static ReciboUpdateResult fromPago(ReciboPagoResponseDto pagoResponse) {
        return ReciboUpdateResult.builder().pagoResponse(pagoResponse).build();
    }
}
