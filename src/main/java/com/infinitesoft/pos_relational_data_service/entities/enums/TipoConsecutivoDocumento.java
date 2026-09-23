package com.infinitesoft.pos_relational_data_service.entities.enums;

import lombok.Getter;

@Getter
public enum TipoConsecutivoDocumento {
    VENTA("VTA"),
    NC("NC"),
    ND("ND"),
    MOV_INVENTARIO("MINV");

    private final String prefijo;

    TipoConsecutivoDocumento(String prefijo) {
        this.prefijo = prefijo;
    }
}
