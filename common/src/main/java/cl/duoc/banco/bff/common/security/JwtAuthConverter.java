package cl.duoc.banco.bff.common.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Convierte un {@link Jwt} en un {@link JwtAuthenticationToken} mapeando el claim
 * "authorities" (lista de roles) a GrantedAuthority. Asi cada canal puede exigir
 * su propio rol (ROLE_WEB, ROLE_MOBILE, ROLE_ATM) en las reglas de autorizacion.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    public static final String AUTHORITIES_CLAIM = "authorities";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(AUTHORITIES_CLAIM);
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
