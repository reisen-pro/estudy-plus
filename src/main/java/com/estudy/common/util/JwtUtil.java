package com.estudy.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    /**
     * 密钥
     */
    @Value("${estudy.jwt.secret}")
    private String secret;

    /**
     * 过期时间
     */
    @Value("${estudy.jwt.expiration}")
    private long expiration;

    /**
     * 这是 HMAC-SHA 算法（基于哈希的消息认证码）。
     * Keys.hmacShaKeyFor() 是 JJWT 库提供的方法，用于将字节数组转换为适合 HMAC-SHA 签名算法的密钥。
     * 它会根据密钥长度自动选择合适的 SHA 变体（如 HMAC-SHA256、HMAC-SHA384 或 HMAC-SHA512），用于 JWT Token 的签名和验证。
     * 安全风险
     * HMAC-SHA 的风险：
     * 密钥泄露风险：如果 secret 被窃取，攻击者可伪造任意 Token
     * 密钥强度不足：弱密钥易被暴力破解
     * 无法公开验证：验证方必须持有相同密钥
     * 建议
     * 当前项目使用 HMAC-SHA 是合适的，因为：
     * 学习平台通常是单体架构
     * 配置简单，性能好
     * 只需确保密钥足够强（建议 256 位以上）
     * 需要考虑切换到 RSA/ECDSA 的场景：
     * 微服务架构需要多服务验证 Token
     * 需要第三方应用验证但不允许其签发 Token
     * OAuth2 授权服务器
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username) {
        // 声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析Token
     * 解密的过程
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 校验Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // TODO: 考虑实现 Refresh Token 双 Token 机制
    // - Access Token: 短期有效（30分钟），用于接口认证
    // - Refresh Token: 长期有效（7-30天），用于刷新 Access Token
    // - 优势：既保证安全性，又避免用户频繁登录
}
