package cl.duoc.banco.bff.atm.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.common.dto.LoginRequest;
import cl.duoc.banco.bff.common.dto.LoginResponse;
import cl.duoc.banco.bff.common.dto.OAuth2TokenResponse;
import cl.duoc.banco.bff.common.security.OAuth2TokenClient;
import lombok.RequiredArgsConstructor;

/**
 * Autenticacion del canal ATM mediante OAuth 2.0 (client credentials).
 * El BFF solicita un access token al Authorization Server usando su cliente
 * de canal; el token emitido lleva el rol ROLE_ATM.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String CHANNEL = "ATM";

    private final OAuth2TokenClient oauth2TokenClient;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody(required = false) LoginRequest request) {
        OAuth2TokenResponse token = oauth2TokenClient.obtainToken();
        return new LoginResponse(token.accessToken(), "Bearer", CHANNEL, token.expiresIn());
    }
}
