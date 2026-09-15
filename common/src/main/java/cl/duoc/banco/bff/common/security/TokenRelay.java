package cl.duoc.banco.bff.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utilidad para propagar (relay) el token JWT del contexto de seguridad actual
 * hacia las llamadas que un BFF hace a backend-core.
 */
public final class TokenRelay {

    private TokenRelay() {
    }

    /**
     * Devuelve el valor del token JWT presente en el contexto de seguridad,
     * o {@code null} si no existe una autenticacion de tipo JWT.
     */
    public static String currentTokenValue() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return null;
    }
}
