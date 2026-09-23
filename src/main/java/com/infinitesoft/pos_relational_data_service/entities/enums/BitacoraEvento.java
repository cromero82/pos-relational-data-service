package com.infinitesoft.pos_relational_data_service.entities.enums;

import lombok.Getter;

@Getter
public enum BitacoraEvento {
    REG_PROD("REG_PROD"),
    MOD_PROD("MOD_PROD"),
    MOD_PRC_PROD("MOD_PRC_PROD"),
    ENTRADA_INV_PRECIO("ENTRADA_INV_PRECIO"),
    DESHAB_PROD("DESHAB_PROD"),
    HAB_PROD("HAB_PROD");

    private final String sigla;

    BitacoraEvento(String sigla) {
        this.sigla = sigla;
    }
}
