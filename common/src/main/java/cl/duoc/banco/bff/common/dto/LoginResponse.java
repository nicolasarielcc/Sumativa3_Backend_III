package cl.duoc.banco.bff.common.dto;

/**
 * Respuesta de login con el token de acceso emitido para el canal.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        String channel,
        long expiresInSeconds) {

    public static LoginResponse of(String accessToken, String channel, long expiresInSeconds) {
        return new LoginResponse(accessToken, "Bearer", channel, expiresInSeconds);
    }
}
