package com.cerebrus.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolationException;

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
         * Maneja errores de cuota agotada de proveedores externos de IA
         * Retorna 429 Too Many Requests
         */
        @ExceptionHandler(QuotaExceededException.class)
        public ResponseEntity<?> handleQuotaExceeded(
                        QuotaExceededException ex,
                        WebRequest request) {

                Map<String, Object> body = new HashMap<>();
                body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
                body.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "Se ha alcanzado el límite de peticiones");

                return ResponseEntity
                                .status(HttpStatus.TOO_MANY_REQUESTS)
                                .body(body);
        }

        /**
         * Maneja excepciones de acceso denegado
         * Retorna 403 Forbidden
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<?> handleAccessDenied(
                        AccessDeniedException ex,
                        WebRequest request) {

                Map<String, Object> body = new HashMap<>();
                body.put("status", HttpStatus.FORBIDDEN.value());
                body.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "Acceso denegado");

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(body);
        }

    /**
     * Maneja violaciones de integridad de datos (valores demasiado grandes, duplicados, etc.)
     * Retorna 422 Unprocessable Entity
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());

        String rootMsg = ex.getMostSpecificCause().getMessage();
        if (rootMsg != null && rootMsg.toLowerCase().contains("data truncation")) {
            body.put("mensaje", "Algún campo excede el tamaño máximo permitido. Reduce el texto o el valor e inténtalo de nuevo.");
        } else if (rootMsg != null && rootMsg.toLowerCase().contains("out of range")) {
            body.put("mensaje", "El valor numérico introducido es demasiado grande.");
        } else if (rootMsg != null && rootMsg.toLowerCase().contains("duplicate")) {
            body.put("mensaje", "Ya existe un registro con esos datos.");
        } else {
            body.put("mensaje", "Los datos introducidos no son válidos. Revisa los campos e inténtalo de nuevo.");
        }

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    /**
     * Maneja errores de deserialización JSON (ej: número demasiado grande para Integer)
     * Retorna 422 Unprocessable Entity
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && Number.class.isAssignableFrom(ife.getTargetType())) {
            String field = ife.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .collect(Collectors.joining("."));
            body.put("mensaje", "El valor numérico de '" + field + "' es demasiado grande.");
        } else {
            body.put("mensaje", "Los datos enviados no tienen un formato válido. Revisa los campos e inténtalo de nuevo.");
        }

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

            /**
             * Maneja parámetros de request obligatorios ausentes
             * Retorna 400 Bad Request
             */
            @ExceptionHandler(MissingServletRequestParameterException.class)
            public ResponseEntity<?> handleMissingServletRequestParameter(
                MissingServletRequestParameterException ex,
                WebRequest request) {

            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("mensaje", "Faltan parámetros obligatorios en la petición");

            Map<String, String> errores = new HashMap<>();
            errores.put(ex.getParameterName(), "Parámetro obligatorio ausente");
            body.put("errores", errores);

            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
            }

            /**
             * Maneja parámetros con tipo inválido (ej: maestroId=abc)
             * Retorna 400 Bad Request
             */
            @ExceptionHandler(MethodArgumentTypeMismatchException.class)
            public ResponseEntity<?> handleMethodArgumentTypeMismatch(
                MethodArgumentTypeMismatchException ex,
                WebRequest request) {

            Map<String, Object> body = new HashMap<>();
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("mensaje", "Formato de parámetro inválido");

            Map<String, String> errores = new HashMap<>();
            String targetType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "tipo esperado";
            errores.put(ex.getName(), "Valor inválido. Se esperaba: " + targetType);
            body.put("errores", errores);

            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
            }

    /**
     * Maneja métodos HTTP no soportados para el endpoint solicitado
     * Retorna 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        body.put("mensaje", "Método HTTP no permitido para este endpoint");

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
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