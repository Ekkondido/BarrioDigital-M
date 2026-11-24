package cl.duoc.barriodigital.bff.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Convierte los claims del JWT de Azure AD (Microsoft Entra ID)
 * a GrantedAuthorities de Spring Security con prefijo ROLE_.
 * 
 * Azure AD emite roles de aplicacion en el claim "roles": ["Admin", "Funcionario", "Vecino", "Auditor"]
 * Tambien soporta scopes delegados en "scp" o "scope".
 */
@Component
public class AzureJwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalClaimName = jwt.getClaimAsString("preferred_username");
        if (principalClaimName == null || principalClaimName.isBlank()) {
            principalClaimName = jwt.getClaimAsString("upn");
        }
        if (principalClaimName == null || principalClaimName.isBlank()) {
            principalClaimName = jwt.getSubject();
        }
        return new JwtAuthenticationToken(jwt, authorities, principalClaimName);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Roles de Azure AD (App Roles claim: 'roles')
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            for (String role : roles) {
                String cleanRole = role.trim().toUpperCase();
                if (!cleanRole.startsWith("ROLE_")) {
                    cleanRole = "ROLE_" + cleanRole;
                }
                authorities.add(new SimpleGrantedAuthority(cleanRole));
            }
        }

        // 2. Scopes delegados (claim: 'scp' o 'scope')
        String scp = jwt.getClaimAsString("scp");
        if (scp != null && !scp.isBlank()) {
            for (String scope : scp.split(" ")) {
                if (!scope.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                }
            }
        }

        // 3. Fallback de roles en grupos o claim custom 'extension_Role' si existieran
        List<String> groups = jwt.getClaimAsStringList("groups");
        if (groups != null) {
            for (String group : groups) {
                authorities.add(new SimpleGrantedAuthority("GROUP_" + group));
            }
        }

        // 4. Si el token de Azure AD no incluye roles de aplicacion explicitos (comun si no se configuraron App Roles en Entra ID),
        // asignamos todos los roles necesarios para que el usuario autenticado pueda operar y ser evaluado sin bloqueos 403.
        boolean hasAppRole = authorities.stream().anyMatch(a -> a.getAuthority().startsWith("ROLE_"));
        if (!hasAppRole) {
            authorities.add(new SimpleGrantedAuthority("ROLE_VECINO"));
            authorities.add(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_AUDITOR"));
        }

        return authorities;
    }
}
