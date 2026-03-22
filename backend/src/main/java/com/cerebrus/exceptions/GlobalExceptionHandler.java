package com.cerebrus.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja validaciones de argumentos de método (DTOs con @Valid)
     * Retorna 422 Unprocessable Entity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("mensaje", "Error de validación: Los datos proporcionados no son válidos");
        
        Map<String, String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage(),
                    (existing, replacement) -> existing + "; " + replacement
                ));
        
        body.put("errores", errores);
        
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    /**
     * Maneja violaciones de restricciones de validación
     * Retorna 422 Unprocessable Entity
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("mensaje", "Error de validación: Los datos proporcionados no son válidos");
        
        Map<String, String> errores = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> violation.getMessage(),
                    (existing, replacement) -> existing + "; " + replacement
                ));
        
        body.put("errores", errores);
        
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    /**
     * Maneja errores de formato de datos (ej: fecha inválida, número inválido)
     * Retorna 422 Unprocessable Entity
     */
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<?> handleInvalidFormat(
            InvalidFormatException ex,
            WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("mensaje", "Error de formato: El tipo de dato no es válido");
        
        Map<String, String> errores = new HashMap<>();
        errores.put(ex.getPath().stream()
                .map(ref -> ref.getFieldName())
                .collect(Collectors.joining(".")), 
                "Formato inválido para el campo. Se esperaba: " + ex.getTargetType().getSimpleName());
        
        body.put("errores", errores);
        
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    /**
     * Maneja excepciones de argumentos ilegales
     * Retorna 422 Unprocessable Entity
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "Datos inválidos");
        
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    /**
     * Maneja excepciones de negocio no encontradas
     * Retorna 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("mensaje", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    /**
     * Maneja excepciones generales
     * Retorna 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("mensaje", "Error interno del servidor");
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
