package com.smartbuilding.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

// Helper class for everything related to JWT tokens.
// A JWT is just a signed piece of text that proves "I am user X".
// The server creates it at login, and checks it on every later request.
@Component
public class JwtUtil {

    // Values below come from application.properties (jwt.secret, jwt.expiration, ...).
    // @Value simply reads a property from that file into the field.
    @Value("${jwt.secret}")
    private String secret;

    // How long an access token stays valid, in milliseconds (1 day).
    @Value("${jwt.expiration}")
    private long expiration;

    // How long a refresh token stays valid, in milliseconds (7 days).
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // The secret key used to SIGN tokens and to VERIFY signatures later.
    // It is built from our secret text. Both sides must use the same secret.
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ---- Creating tokens ----

    // Short-lived token (1 day) sent with every API request.
    public String generateToken(String username, Map<String, Object> extraInfo) {
        return buildToken(username, extraInfo, expiration);
    }

    // Long-lived token (7 days) used only to ask for a fresh access token.
    public String generateRefreshToken(String username, Map<String, Object> extraInfo) {
        return buildToken(username, extraInfo, refreshExpiration);
    }

    // Puts username + extra info + expiry date into a token and signs it.
    private String buildToken(String username, Map<String, Object> extraInfo, long validForMillis) {
        return Jwts.builder()
                .setClaims(extraInfo)                        // e.g. role, userId
                .setSubject(username)                        // "who is this token for"
                .setIssuedAt(new Date())                     // created now
                .setExpiration(new Date(System.currentTimeMillis() + validForMillis))
                .signWith(signingKey())                      // stamp it so nobody can fake it
                .compact();                                  // turn it into a String
    }

    // ---- Reading tokens ----

    // Opens a token, checks its signature, and returns everything stored inside.
    // Throws an exception if the signature is wrong (fake token).
    private Claims readToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Who does this token belong to?
    public String extractUsername(String token) {
        return readToken(token).getSubject();
    }

    // Which user id was stored inside at login?
    public Long extractUserId(String token) {
        return readToken(token).get("userId", Long.class);
    }

    // Has the expiry date passed?
    private boolean isExpired(String token) {
        Date expiry = readToken(token).getExpiration();
        return expiry.before(new Date());
    }

    // A token is usable only if its signature is valid AND it is not expired.
    public boolean validateToken(String token) {
        try {
            readToken(token);          // fails here if signature is bad
            return !isExpired(token);  // fails here if too old
        } catch (Exception e) {
            return false;              // bad signature, expired, or garbage text
        }
    }
}