package net.engineeringdigest.journalApp.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final String SECRET_KEY = System.getProperty("jwt.secret");

    public String generateToken(UserDetails userDetails, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("tokenType", "ACCESS");
        return createToken(claims, userDetails.getUsername(), 1000 * 60 * 15); // 15 minutes
    }

    public String generateRefreshToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");
        return createToken(claims, userName, 1000 * 60 * 30); // 30 minutes
    }

    private String createToken(Map<String, Object> claims, String userName, long expirationMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String extractUsername(String jwt) {
        return extractAllClaims(jwt).getSubject();
    }

    public String extractTokenType(String jwt) {
        return (String) extractAllClaims(jwt).get("tokenType");
    }

    private Date extractExpiration(String jwt) {
        return extractAllClaims(jwt).getExpiration();
    }

    public Boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    public Claims extractAllClaims(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("roles");
    }

}
