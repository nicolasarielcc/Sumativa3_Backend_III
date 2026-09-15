package cl.duoc.banco.bff.atm.controller;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.banco.bff.common.dto.LoginRequest;
import cl.duoc.banco.bff.common.dto.LoginResponse;
import cl.duoc.banco.bff.common.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Autenticacion del canal ATM. Emite un JWT con el rol ROLE_ATM.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String CHANNEL = "ATM";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String token = jwtService.generateToken(authentication.getName(), CHANNEL, roles);
        return LoginResponse.of(token, CHANNEL, jwtService.expirationSeconds());
    }
}
