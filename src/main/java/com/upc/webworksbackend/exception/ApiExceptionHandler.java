package com.upc.webworksbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(Exception exception) {
        String message = (exception instanceof ForbiddenException && exception.getMessage() != null && !exception.getMessage().isBlank())
                ? exception.getMessage()
                : "Acceso denegado: no tiene permisos para realizar esta acción.";
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Datos de la solicitud inválidos.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Cuerpo de la solicitud inválido o malformado."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", exception.getMessage() != null ? exception.getMessage() : "Parámetro inválido."));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Ocurrió un error interno en el servidor."));
    }
}
