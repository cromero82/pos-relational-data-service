package com.infinitesoft.pos_relational_data_service.hibernate;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

public class JsonbH2Dialect extends H2Dialect {

    public JsonbH2Dialect() {
        super();
    }

    @Override
    public String getTypeName(int code, long length, int precision, int scale) {
        if (code == Types.OTHER) {
            return "OTHER";
        }
        return super.getTypeName(code, length, precision, scale);
    }

    @Override
    public String getTypeName(int code) {
        if (code == Types.OTHER) {
            return "OTHER";
        }
        return super.getTypeName(code);
    }

    @Override
    public String getCastTypeName(int code) {
        if (code == Types.OTHER) {
            return "OTHER";
        }
        return super.getCastTypeName(code);
    }
}
