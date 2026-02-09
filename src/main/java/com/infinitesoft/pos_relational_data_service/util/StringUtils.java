package com.infinitesoft.pos_relational_data_service.util;

import java.lang.reflect.Field;

public class StringUtils {

    /**
     * Convierte todos los campos de tipo String de un objeto a mayúsculas.
     * @param object El objeto cuyos campos String se convertirán.
     */
    public static void convertStringsToUpperCase(Object object) {
        if (object == null) {
            return;
        }

        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                try {
                    field.setAccessible(true);
                    String value = (String) field.get(object);
                    if (value != null) {
                        field.set(object, value.toUpperCase());
                    }
                } catch (IllegalAccessException e) {
                    // Ignorar campos que no se pueden acceder
                }
            }
        }
    }
}
