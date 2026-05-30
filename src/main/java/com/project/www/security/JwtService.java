package com.project.www.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "mysecretkeymysecretkeymysecretkey12345";

    private static final long ACCESS_EXPIRATION =
            1000L * 60 * 60 * 24; // 24 hours

    private static final long REFRESH_EXPIRATION =
            1000L * 60 * 60 * 24 * 7; // 7 days

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String email, Long tenantId, String tenantCode) {
        return generateTokenWithExpiration(email, tenantId, tenantCode, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(String email, Long tenantId, String tenantCode) {
        return generateTokenWithExpiration(email, tenantId, tenantCode, REFRESH_EXPIRATION);
    }

    private String generateTokenWithExpiration(String email, Long tenantId, String tenantCode, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", tenantId);
        claims.put("tenantCode", tenantCode);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + expirationMs)
                )
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Long extractTenantId(String token) {
        Object tenantIdObj = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("tenantId");
        
        if (tenantIdObj instanceof Number) {
            return ((Number) tenantIdObj).longValue();
        }
        return null;
    }

    public String extractTenantCode(String token) {
        Object tenantCodeObj = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("tenantCode");
        
        return (tenantCodeObj != null) ? tenantCodeObj.toString() : null;
    }

    public boolean isTokenValid(String token, String email) {
        String username = extractUsername(token);
        return username.equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.before(new Date());
    }
}