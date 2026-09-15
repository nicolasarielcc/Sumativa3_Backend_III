package cl.duoc.banco.bff.common.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configuracion de soporte de seguridad compartida por todos los servicios.
 * Provee el {@link JwtDecoder} que valida los tokens HS256 usando el secreto
 * compartido configurado en {@code app.security.jwt-secret}.
 */
@Configuration
public class SecuritySupportConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.security.jwt-secret}") String secret) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
