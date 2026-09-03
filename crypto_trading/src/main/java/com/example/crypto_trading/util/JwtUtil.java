package com.example.crypto_trading.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_TEMP = "temp";
    private static final String TYPE_ACCESS = "access";

    private final SecretKey secretKey;
    private final long tempExpirationMs;
    private final long accessExpirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.temp-expiration-ms}") long tempExpirationMs,
            @Value("${jwt.access-expiration-ms}") long accessExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tempExpirationMs = tempExpirationMs;
        this.accessExpirationMs = accessExpirationMs;
    }

    /**
     * Sinh token tạm (15 phút) dùng sau khi verify OTP thành công.
     * Claim "type" = "temp"
     */
    public String generateTempToken(String email) {
        return buildToken(email, TYPE_TEMP, tempExpirationMs);
    }

    /**
     * Sinh access token chính thức (24h) dùng sau khi hoàn tất đăng ký.
     * Claim "type" = "access"
     */
    public String generateAccessToken(String email) {
        return buildToken(email, TYPE_ACCESS, accessExpirationMs);
    }

    /**
     * Validate token tạm: kiểm tra chữ ký, hạn, và đúng type=temp.
     *
     * @return email nếu hợp lệ, ném exception nếu không
     */
    public String validateTempToken(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_TEMP.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Token không hợp lệ");
        }
        return claims.getSubject();
    }

    /**
     * Lấy email từ bất kỳ token hợp lệ nào.
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private String buildToken(String subject, String type, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .claim(CLAIM_TYPE, type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
