package cl.duoc.banco.bff.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import cl.duoc.banco.bff.common.security.JwtAuthConverter;
import lombok.RequiredArgsConstructor;

/**
 * Configuracion de seguridad de backend-core (resource server JWT).
 * - Lecturas: requieren un JWT valido (cualquier canal autenticado).
 * - Retiro (operacion critica): solo el canal ATM (ROLE_ATM).
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/cuentas/*/retiros").hasAuthority("ROLE_ATM")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }
}
