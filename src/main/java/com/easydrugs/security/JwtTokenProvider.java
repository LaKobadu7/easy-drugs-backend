package com.easydrugs.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Fournisseur de tokens JWT — EASY-DRUGS.
 * Génère, valide et extrait les claims des tokens JWT.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ── Génération ──────────────────────────────────────────────

    /**
     * Génère un token JWT pour un utilisateur.
     * Le subject est le numéro de téléphone (identifiant unique).
     */
    public String genererToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Ajouter le rôle dans les claims pour éviter une requête BDD
        userDetails.getAuthorities().forEach(auth ->
            claims.put("role", auth.getAuthority())
        );
        return buildToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    public String genererRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey())
            .compact();
    }

    // ── Validation ──────────────────────────────────────────────

    public boolean estValide(String token, UserDetails userDetails) {
        final String telephone = extraireTelephone(token);
        return telephone.equals(userDetails.getUsername()) && !estExpire(token);
    }

    public boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    // ── Extraction ──────────────────────────────────────────────

    public String extraireTelephone(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public String extraireRole(String token) {
        return (String) extraireTousClaims(token).get("role");
    }

    public <T> T extraireClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraireTousClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraireTousClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
