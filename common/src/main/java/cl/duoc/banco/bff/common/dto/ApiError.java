package cl.duoc.banco.bff.common.dto;

import java.time.Instant;

/**
 * Estructura de error estandarizada devuelta por todos los servicios.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message) {

    public static ApiError of(int status, String code, String message) {
        return new ApiError(Instant.now(), status, code, message);
    }
}
