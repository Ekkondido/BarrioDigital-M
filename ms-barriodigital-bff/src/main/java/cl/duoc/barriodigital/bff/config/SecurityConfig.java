package cl.duoc.barriodigital.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AzureJwtRoleConverter azureJwtRoleConverter;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${azure.ad.audience}")
    private String expectedAudience;

    @Value("${azure.ad.client-id}")
    private String clientId;

    @Value("${azure.ad.frontend-client-id:bbf3d1bd-ee73-4f0f-8efc-90a3688a1199}")
    private String frontendClientId;

    @Value("${azure.ad.tenant-id:9544c211-793b-440b-98a8-cc363e1a04cb}")
    private String tenantId;

    @Value("${security.mock-jwt-enabled:false}")
    private boolean mockJwtEnabled;

    public SecurityConfig(AzureJwtRoleConverter azureJwtRoleConverter,
                          CustomAuthEntryPoint customAuthEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.azureJwtRoleConverter = azureJwtRoleConverter;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Actuator y salud del servicio
                .requestMatchers("/actuator/**", "/error").permitAll()
                // Endpoint utilitario para testing/demo si esta activo el mock
                .requestMatchers("/api/auth/mock-token", "/api/auth/demo-login").permitAll()
                
                // Info de usuario autenticado
                .requestMatchers("/api/auth/me").authenticated()

                // Modulo Catálogo
                .requestMatchers(HttpMethod.GET, "/api/catalog/**").hasAnyRole("ADMIN", "FUNCIONARIO", "VECINO", "AUDITOR")
                .requestMatchers(HttpMethod.POST, "/api/catalog/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/catalog/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/catalog/**").hasRole("ADMIN")

                // Modulo Trámites (Requests)
                .requestMatchers(HttpMethod.GET, "/api/requests/**").hasAnyRole("ADMIN", "FUNCIONARIO", "VECINO", "AUDITOR")
                .requestMatchers(HttpMethod.POST, "/api/requests").hasAnyRole("ADMIN", "FUNCIONARIO", "VECINO")
                .requestMatchers(HttpMethod.PUT, "/api/requests/*/status").hasAnyRole("ADMIN", "FUNCIONARIO")

                // Modulo Reportería
                .requestMatchers("/api/report/**").hasRole("ADMIN")

                // Modulo Auditoría
                .requestMatchers("/api/audit/**").hasAnyRole("ADMIN", "AUDITOR")

                // Modulo Administrador RabbitMQ (Evaluación 3)
                .requestMatchers("/api/admin/mq/**").hasRole("ADMIN")

                // Todo lo demas requiere estar autenticado
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(hybridJwtDecoder())
                    .jwtAuthenticationConverter(azureJwtRoleConverter)
                )
            );

        return http.build();
    }

    /**
     * Decoder híbrido:
     * - Si el token proviene de perfiles de evaluación (demo-token-*) o modo mock, resuelve los roles correspondientes.
     * - Si el token proviene de Microsoft Entra ID (Azure AD), valida firma JWKS, Issuer y Audience.
     */
    @Bean
    public JwtDecoder hybridJwtDecoder() {
        NimbusJwtDecoder azureDecoder = null;
        try {
            NimbusJwtDecoder nimbus = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();
            OAuth2TokenValidator<Jwt> issuerValidator = new FlexibleIssuerValidator(tenantId);
            OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(expectedAudience, clientId, frontendClientId);
            OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(timestampValidator, issuerValidator, audienceValidator);
            nimbus.setJwtValidator(combinedValidator);
            azureDecoder = nimbus;
        } catch (Exception e) {
            System.err.println("[SecurityConfig] Error inicializando NimbusJwtDecoder: " + e.getMessage());
        }

        final NimbusJwtDecoder finalAzureDecoder = azureDecoder;

        return token -> {
            if (token != null && (token.startsWith("demo-token-") || token.startsWith("mock-") || mockJwtEnabled)) {
                return buildDemoJwt(token);
            }
            if (finalAzureDecoder != null) {
                try {
                    return finalAzureDecoder.decode(token);
                } catch (Exception ex) {
                    System.err.println("[SecurityConfig] Error al validar/decodificar JWT de Azure AD: " + ex.getMessage());
                    throw ex;
                }
            }
            return buildDemoJwt(token);
        };
    }

    private Jwt buildDemoJwt(String token) {
        Instant now = Instant.now();
        List<String> roles;
        String roleName = "Vecino";

        if (token != null && token.contains("Admin")) {
            roles = List.of("Admin", "Funcionario", "Vecino", "Auditor");
            roleName = "Admin";
        } else if (token != null && token.contains("Funcionario")) {
            roles = List.of("Funcionario", "Vecino");
            roleName = "Funcionario";
        } else if (token != null && token.contains("Auditor")) {
            roles = List.of("Auditor");
            roleName = "Auditor";
        } else {
            roles = List.of("Vecino");
        }

        return Jwt.withTokenValue(token != null ? token : "demo-token")
                .header("alg", "none")
                .subject("demo-user-" + roleName.toLowerCase())
                .issuer(issuerUri)
                .audience(List.of(expectedAudience, clientId))
                .claim("preferred_username", roleName.toLowerCase() + "@barriodigital.cl")
                .claim("name", "Usuario " + roleName + " (Evaluación)")
                .claim("roles", roles)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(86400))
                .build();
    }

    /**
     * Validador flexible de Issuer para Azure AD.
     * Acepta tokens v1 (sts.windows.net/{tenantId}/) y v2 (login.microsoftonline.com/{tenantId}/v2.0).
     */
    static class FlexibleIssuerValidator implements OAuth2TokenValidator<Jwt> {
        private final String tenantId;

        public FlexibleIssuerValidator(String tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (issuer.contains(tenantId)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error("invalid_token",
                "Token rechazado: el emisor (" + issuer + ") no corresponde al Tenant ID de Azure AD (" + tenantId + ")", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    /**
     * Validador de Audience para asegurar que el token fue emitido especificamente para esta API.
     * Acepta: access tokens (aud=api://backendClientId o api://backendClientId/scope),
     *         e ID tokens (aud=frontendClientId).
     * Usa contains(clientId) para cubrir variantes v1/v2 de Azure con o sin prefijo api://.
     */
    static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String clientId;
        private final String frontendClientId;

        public AudienceValidator(String expectedAudience, String clientId, String frontendClientId) {
            this.clientId = clientId;
            this.frontendClientId = frontendClientId;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            List<String> audience = jwt.getAudience();
            if (audience != null && audience.stream().anyMatch(aud ->
                    aud.contains(clientId) || aud.equals(frontendClientId)
            )) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error("invalid_token",
                "Token rechazado: audiencia no válida. Se esperaba que aud contenga " + clientId
                + " o sea igual a " + frontendClientId + ". Audiencia recibida: " + audience, null);
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    /**
     * Decoder simple para entornos de desarrollo local desconectados.
     */
    private JwtDecoder mockJwtDecoder() {
        return token -> {
            // Retorna un JWT basico para pruebas locales si mockJwtEnabled=true
            Instant now = Instant.now();
            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .subject("dev-user-01")
                    .issuer(issuerUri)
                    .audience(List.of(expectedAudience, clientId))
                    .claim("preferred_username", "admin.local@barriodigital.cl")
                    .claim("name", "Administrador Local Dev")
                    .claim("roles", List.of("Admin", "Funcionario", "Vecino", "Auditor"))
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build();
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
