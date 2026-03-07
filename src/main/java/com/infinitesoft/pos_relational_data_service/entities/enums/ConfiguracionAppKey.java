package com.infinitesoft.pos_relational_data_service.entities.enums;

public enum ConfiguracionAppKey {
    TOTAL_PRODUCTOS("total-productos");

    private final String key;

    ConfiguracionAppKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
