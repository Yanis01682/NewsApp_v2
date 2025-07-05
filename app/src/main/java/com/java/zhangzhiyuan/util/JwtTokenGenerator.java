package com.java.zhangzhiyuan.util;

import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtTokenGenerator {

    /**
     * 手动构建符合智谱AI V4规范的JWT Token。
     * 此版本不依赖任何第三方JWT库，直接使用安卓原生API进行构建，
     * 从而完全绕开所有外部库的密钥强度检查，是解决WeakKeyException的最终方案。
     *
     * @param apiKey 您的API Key，格式为 "id.secret"
     * @param expMillis Token的有效期，单位毫秒
     * @return 生成的JWT Token字符串
     */
    public static String generateToken(String apiKey, long expMillis) {
        String[] parts = apiKey.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid API Key format. Expected 'id.secret'");
        }
        String id = parts[0];
        String secret = parts[1];

        try {
            // 1. 创建JWT Header
            JSONObject header = new JSONObject();
            header.put("alg", "HS256");
            header.put("sign_type", "SIGN");

            // 2. 创建JWT Payload
            JSONObject payload = new JSONObject();
            payload.put("api_key", id);
            payload.put("exp", System.currentTimeMillis() + expMillis);
            payload.put("timestamp", System.currentTimeMillis());

            // 3. 对Header和Payload进行Base64Url编码
            // Base64.URL_SAFE: 使用URL安全的字母表
            // Base64.NO_PADDING: 不添加额外的'='填充
            // Base64.NO_WRAP: 不添加换行符
            int flags = Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP;
            String encodedHeader = Base64.encodeToString(header.toString().getBytes(StandardCharsets.UTF_8), flags);
            String encodedPayload = Base64.encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8), flags);

            // 4. 创建待签名字符串
            String signingInput = encodedHeader + "." + encodedPayload;

            // 5. 使用HmacSHA256进行签名
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));

            // 6. 对签名进行Base64Url编码
            String encodedSignature = Base64.encodeToString(signatureBytes, flags);

            // 7. 拼接成最终的JWT
            return signingInput + "." + encodedSignature;

        } catch (Exception e) {
            // 在实际应用中，这里应该有更完善的异常处理
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }
}