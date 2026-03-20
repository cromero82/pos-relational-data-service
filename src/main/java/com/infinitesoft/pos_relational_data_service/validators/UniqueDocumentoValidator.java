package com.infinitesoft.pos_relational_data_service.validators;

import com.infinitesoft.pos_relational_data_service.repositories.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueDocumentoValidator implements ConstraintValidator<UniqueDocumento, String> {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Override
    public void initialize(UniqueDocumento constraintAnnotation) {
    }

    @Override
    public boolean isValid(String documento, ConstraintValidatorContext context) {
        if (proveedorRepository == null) {
            return true; // No se puede validar en este contexto (ej. durante la inicialización)
        }
        return !proveedorRepository.findByDocumento(documento).isPresent();
    }
}
