package cl.duoc.banco.bff.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.banco.bff.common.dto.ApiError;

/**
 * Manejo centralizado y estandarizado de errores (cuerpo JSON predecible).
 * Aplica a todos los servicios que escanean el paquete base
 * {@code cl.duoc.banco.bff}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "UNAUTHORIZED", "Credenciales invalidas"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        int code = status != null ? status.value() : 500;
        ApiError error = ApiError.of(code, ex.getReason() != null ? ex.getReason() : "ERROR", ex.getReason());
        return ResponseEntity.status(code).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Solicitud invalida");
        return ResponseEntity.badRequest().body(ApiError.of(400, "VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(ApiError.of(500, "INTERNAL_ERROR", "Error interno del servidor"));
    }
}
