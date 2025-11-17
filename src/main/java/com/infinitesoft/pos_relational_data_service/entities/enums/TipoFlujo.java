package com.infinitesoft.pos_relational_data_service.entities.enums;

public enum TipoFlujo {
    INGRESO(1),
    EGRESO(2);

    private final int id;

    TipoFlujo(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
