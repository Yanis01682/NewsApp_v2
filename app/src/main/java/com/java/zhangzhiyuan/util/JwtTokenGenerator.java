package com.java.zhangzhiyuan.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenGenerator {

    public static final String API_KEY = "805c28b7b77747a88617bfb68e53aca8.FJK3H1Frt1nhGVvR"; // 确保这里是你自己的Key

    public static String generateToken(String apiKey, long expMillis) {
        String[] parts = apiKey.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid API Key format");
        }
        String id = parts[0];
        String secret = parts[1];

        Map<String, Object> claims = new HashMap<>();
        claims.put("api_key", id);
        claims.put("exp", System.currentTimeMillis() + expMillis);
        claims.put("timestamp", System.currentTimeMillis());

        // 使用老版本的签名方式
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }
}