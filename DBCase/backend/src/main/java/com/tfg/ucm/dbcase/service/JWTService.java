package com.tfg.ucm.dbcase.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String key;

    public String generateToken(String username) {

        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaimUnsafe(token, Claims::getSubject);
    }

    public Optional<String> extractUsernameSafe(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaimUnsafe(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(extractAllClaimsUnsafe(token));
    }

    private <T> Optional<T> extractClaim(String token, Function<Claims, T> claimResolver) {
        return extractAllClaims(token).map(claimResolver);
    }

    private Claims extractAllClaimsUnsafe(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

    private Optional<Claims> extractAllClaims(String token) {
        try {
            return Optional.of(extractAllClaimsUnsafe(token));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsernameSafe(token)
                .filter(username -> username.equals(userDetails.getUsername()))
                .map(username -> !isTokenExpired(token))
                .orElse(false);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration).orElse(new Date(0));
    }
}
