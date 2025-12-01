package com.moveit.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service de gestion des JWT (JSON Web Tokens).
 */
@Service
public class JwtService {

    private final String secretKey;
    private final long jwtExpiration;

    /**
     * Constructeur du service JwtService.
     */
    public JwtService(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long jwtExpiration) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
    }

    /**
     * Extrait le nom d'utilisateur du token JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait une information spécifique du token JWT.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un token JWT pour un utilisateur.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token JWT avec des informations supplémentaires.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        if (extraClaims == null) {
            extraClaims = new HashMap<>();
        }
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Récupère le temps d'expiration des tokens.
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Construit un token JWT.
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Vérifie si un token JWT est valide pour un utilisateur.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (RuntimeException e) {
            return false; // Token mal formé, signature invalide, expiré, etc.
        }
    }

    /**
     * Vérifie si un token JWT est expiré.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration d'un token JWT.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait toutes les informations d'un token JWT.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SecurityException e) {
            throw new RuntimeException("Signature JWT invalide", e);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de parser le token JWT", e);
        }
    }

    /**
     * Récupère la clé de signature pour les tokens JWT.
     */
    private Key getSignInKey() {
        // On suppose que la secretKey est Base64; si ce n'est pas le cas, remplacer par secretKey.getBytes(StandardCharsets.UTF_8)
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}