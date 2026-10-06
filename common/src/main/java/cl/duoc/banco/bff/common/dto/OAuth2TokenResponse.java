package cl.duoc.banco.bff.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Respuesta del token endpoint del Authorization Server OAuth 2.0.
 */
public record OAuth2TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("scope") String scope) {
}
