package cl.duoc.banco.bff.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import cl.duoc.banco.bff.common.dto.OAuth2TokenResponse;

/**
 * Cliente OAuth 2.0 (grant type {@code client_credentials}). Cada BFF usa sus
 * propias credenciales de cliente (una por canal) para obtener un access token
 * del Authorization Server. El token resultante lleva el rol del canal.
 *
 * <p>Se registra unicamente si la propiedad {@code app.oauth2.token-uri} esta
 * presente (solo en los BFFs, que actuan como clientes OAuth 2.0; backend-core
 * es solo resource server y no la define).</p>
 */
@Component
@ConditionalOnProperty(name = "app.oauth2.token-uri")
public class OAuth2TokenClient {

    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    public OAuth2TokenClient(
            @Value("${app.oauth2.token-uri}") String tokenUri,
            @Value("${app.oauth2.client-id}") String clientId,
            @Value("${app.oauth2.client-secret}") String clientSecret,
            @Value("${app.oauth2.scope}") String scope) {
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    /**
     * Solicita un access token mediante client credentials (HTTP Basic).
     */
    public OAuth2TokenResponse obtainToken() {
        String basic = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        return RestClient.create()
                .post()
                .uri(tokenUri)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials&scope=" + scope)
                .retrieve()
                .body(OAuth2TokenResponse.class);
    }
}
