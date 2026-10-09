package com.pod.back.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${secretKey}")
    private String secretKey;

    @Value("${JwtExpiration}") // 24h par défaut (ms)
    private long jwtExpiration;

    private long getExpirationInMs() {
        return jwtExpiration * 60 * 1000;
    }
    // 1. Génération du token basique
    public String generateToken(UserDetails userDetails) {
        return generateToken(Map.of(), userDetails);
    }

    // 2. Génération du token avec Claims personnalisés
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        var builder = JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + getExpirationInMs()));

        // Ajout des claims additionnels éventuels
        extraClaims.forEach((key, value) -> {
            if (value instanceof String s) builder.withClaim(key, s);
            else if (value instanceof Integer i) builder.withClaim(key, i);
            else if (value instanceof Boolean b) builder.withClaim(key, b);
        });

        return builder.sign(Algorithm.HMAC256(secretKey));
    }

    // 3. Extraction de l'email/username
    public String extractUsername(String token) {
        return decodeToken(token).getSubject();
    }

    // 4. Validation du token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return decodeToken(token).getExpiresAt().before(new Date());
    }

    // Décodage et vérification de la signature HMAC256
    private DecodedJWT decodeToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        return verifier.verify(token);
    }
}