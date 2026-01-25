package com.infinitesoft.pos_relational_data_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BitacoraUsuarioRequest {
    private Integer eventoId;
    private String evento;
    private String valorAntes;
    private String valorDespues;
    private Integer referenciaId;
}
