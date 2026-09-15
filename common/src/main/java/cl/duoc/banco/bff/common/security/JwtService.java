package cl.duoc.banco.bff.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * Emisor de tokens JWT (HS256) con Nimbus JOSE+JWT.
 * El secreto es compartido entre todos los servicios (BFFs y backend-core),
 * de modo que un token emitido por un BFF es validado por los demas.
 */
@Component
public class JwtService {

    private final byte[] secret;
    private final long expirationMs;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-ms:3600000}") long expirationMs,
            @Value("${app.security.jwt-issuer:banco-xyz-bff}") String issuer) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    /**
     * Genera un JWT firmado con los roles/autoridades del canal correspondiente.
     *
     * @param username  usuario autenticado del canal
     * @param channel   canal al que pertenece (WEB, MOBILE, ATM)
     * @param roles     autoridades a incluir (ej. ROLE_WEB)
     */
    public String generateToken(String username, String channel, List<String> roles) {
        try {
            Date now = new Date();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issuer(issuer)
                    .issueTime(now)
                    .expirationTime(new Date(now.getTime() + expirationMs))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("channel", channel)
                    .claim("authorities", roles)
                    .build();

            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJwt.sign(new MACSigner(secret));
            return signedJwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token JWT", e);
        }
    }

    /** Duracion del token en segundos (para informarla en la respuesta de login). */
    public long expirationSeconds() {
        return expirationMs / 1000;
    }
}
