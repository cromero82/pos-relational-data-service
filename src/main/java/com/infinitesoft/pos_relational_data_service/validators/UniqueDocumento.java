package com.infinitesoft.pos_relational_data_service.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueDocumentoValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueDocumento {
    String message() default "El documento ya está registrado";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
