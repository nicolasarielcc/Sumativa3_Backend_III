package cl.duoc.banco.bff.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Peticion de login por canal.
 */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contrasena es obligatoria") String password) {
}
