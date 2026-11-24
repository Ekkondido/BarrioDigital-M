package cl.duoc.barriodigital.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("subject", jwt.getSubject());
        userInfo.put("preferred_username", jwt.getClaimAsString("preferred_username"));
        userInfo.put("name", jwt.getClaimAsString("name"));
        userInfo.put("email", jwt.getClaimAsString("email") != null ? jwt.getClaimAsString("email") : jwt.getClaimAsString("upn"));
        userInfo.put("roles", jwt.getClaimAsStringList("roles") != null ? jwt.getClaimAsStringList("roles") : List.of());
        userInfo.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : "");
        userInfo.put("audience", jwt.getAudience());
        userInfo.put("expiresAt", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : "");

        return ResponseEntity.ok(userInfo);
    }
}
