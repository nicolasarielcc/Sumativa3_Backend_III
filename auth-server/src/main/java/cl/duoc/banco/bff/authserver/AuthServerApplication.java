package cl.duoc.banco.bff.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Authorization Server OAuth 2.0 del Banco XYZ.
 *
 * <p>Centraliza la emision de access tokens (JWT firmados con clave RSA).
 * Los microservicios (backend-core y los BFFs) validan esos tokens mediante
 * el JWK Set expuesto en {@code /oauth2/jwks}.</p>
 */
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
