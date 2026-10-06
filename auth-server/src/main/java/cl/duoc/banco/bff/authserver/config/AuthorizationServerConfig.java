package cl.duoc.banco.bff.authserver.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Configuracion del Authorization Server OAuth 2.0.
 *
 * <p>Registra un cliente por canal (web, mobile, atm) usando el grant type
 * {@code client_credentials}. Cada token emitido incluye el claim
 * {@code authorities} (rol del canal) para que los resource servers puedan
 * aplicar autorizacion por canal.</p>
 */
@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer.oidc(Customizer.withDefaults()))
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/jwks", "/.well-known/openid-configuration",
                                "/.well-known/oauth-authorization-server").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder encoder) {
        RegisteredClient web = client("bff-web-client", "web-secret", "web", encoder);
        RegisteredClient mobile = client("bff-mobile-client", "mobile-secret", "mobile", encoder);
        RegisteredClient atm = client("bff-atm-client", "atm-secret", "atm", encoder);
        return new InMemoryRegisteredClientRepository(web, mobile, atm);
    }

    private RegisteredClient client(String clientId, String secret, String scope, PasswordEncoder encoder) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(encoder.encode(secret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(scope)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el par de claves RSA", e);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(
            org.springframework.kafka.core.KafkaTemplate<String, cl.duoc.banco.bff.authserver.dto.AuthEvent> kafkaTemplate) {
        return context -> {
            if (AuthorizationGrantType.CLIENT_CREDENTIALS.equals(context.getAuthorizationGrantType())) {
                Set<String> scopes = context.getAuthorizedScopes();
                String scope = scopes.stream().findFirst().orElse("");
                String channel = scope.toUpperCase();
                context.getClaims().claim("channel", channel);
                context.getClaims().claim("authorities", List.of("ROLE_" + channel));

                String clientId = context.getRegisteredClient() != null
                        ? context.getRegisteredClient().getClientId()
                        : "desconocido";
                publicarEventoAutenticacion(kafkaTemplate, clientId, channel);
            }
        };
    }

    private void publicarEventoAutenticacion(
            org.springframework.kafka.core.KafkaTemplate<String, cl.duoc.banco.bff.authserver.dto.AuthEvent> kafkaTemplate,
            String clientId, String channel) {
        try {
            cl.duoc.banco.bff.authserver.dto.AuthEvent evento =
                    new cl.duoc.banco.bff.authserver.dto.AuthEvent(
                            clientId, channel, java.time.Instant.now().toString());
            kafkaTemplate.send(KafkaProducerConfig.TOPIC, evento)
                    .whenComplete((r, ex) -> {
                        if (ex != null) {
                            System.err.println("No se pudo publicar el evento de autenticacion: " + ex.getMessage());
                        }
                    });
        } catch (Exception e) {
            System.err.println("Error al publicar evento de autenticacion: " + e.getMessage());
        }
    }
}
