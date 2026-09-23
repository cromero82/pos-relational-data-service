package com.infinitesoft.pos_relational_data_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurarTicketResponseDto {

    private String notaCreditoConsecutivo;
    private Long notaAjusteId;
    private Long ticketId;
    private Long reciboId;
    private String documentoVentaConsecutivoAnulado;
}
