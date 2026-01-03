package com.infinitesoft.pos_relational_data_service.entities;

public enum TipoConflicto {
    DOS_PRODUCTOS_NOMBRES_IGUALES(1),
    IGUAL_NOMBRE_Y_CODIGO_BARRAS(2);

    private final int id;

    TipoConflicto(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TipoConflicto fromId(int id) {
        for (TipoConflicto type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid TipoConflicto id: " + id);
    }
}
