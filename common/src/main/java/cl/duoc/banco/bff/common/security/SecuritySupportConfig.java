package cl.duoc.banco.bff.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configuracion de soporte de seguridad compartida por todos los resource
 * servers. Provee el {@link JwtDecoder} que valida los access tokens JWT
 * emitidos por el Authorization Server (OAuth 2.0) mediante su JWK Set.
 */
@Configuration
public class SecuritySupportConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.security.jwt.jwk-set-uri}") String jwkSetUri) {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
