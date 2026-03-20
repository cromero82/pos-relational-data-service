package com.infinitesoft.pos_relational_data_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<Map<String, String>>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errorsList = new ArrayList<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("campo", error.getField());
            errorMap.put("descripcionError", error.getDefaultMessage());
            errorsList.add(errorMap);
        });

        Map<String, List<Map<String, String>>> response = new HashMap<>();
        response.put("errores", errorsList);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, List<Map<String, String>>>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        List<Map<String, String>> errorsList = new ArrayList<>();
        
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("campo", "base_de_datos");
        errorMap.put("descripcionError", "Error de integridad de datos: " + (ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage()));
        errorsList.add(errorMap);

        Map<String, List<Map<String, String>>> response = new HashMap<>();
        response.put("errores", errorsList);
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, List<Map<String, String>>>> handleEntityNotFoundException(EntityNotFoundException ex) {
        List<Map<String, String>> errorsList = new ArrayList<>();

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("campo", "recurso");
        errorMap.put("descripcionError", ex.getMessage());
        errorsList.add(errorMap);

        Map<String, List<Map<String, String>>> response = new HashMap<>();
        response.put("errores", errorsList);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
