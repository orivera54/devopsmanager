package com.example.azuredevopsdashboard.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Using @Value for constructor injection is cleaner for final fields
    private final SecretKey jwtSecretKey;
    private final int jwtExpirationInMs;

    // Constructor-based injection for @Value properties
    public JwtTokenProvider(@Value("${app.jwtSecret:}") String jwtSecretString, // Default to empty if not set
                            @Value("${app.jwtExpirationInMs:86400000}") int jwtExpirationInMs) { // Default to 24h
        this.jwtExpirationInMs = jwtExpirationInMs;
        if (jwtSecretString == null || jwtSecretString.trim().isEmpty()) {
            logger.warn("app.jwtSecret is not configured. Generating a new secure key. This key will change on each application restart.");
            this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Generates a strong key
        } else if (jwtSecretString.getBytes().length < 64) { // HS512 requires a key of at least 512 bits (64 bytes)
             logger.warn("app.jwtSecret is too short (less than 64 bytes for HS512). Generating a new secure key for HS512. This key will change on each application restart.");
             this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
        else {
            this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
        }
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) { // Changed from SignatureException for broader catch
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }  catch (SignatureException ex) { // Specifically for signature issues
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        }
        return false;
    }
}
