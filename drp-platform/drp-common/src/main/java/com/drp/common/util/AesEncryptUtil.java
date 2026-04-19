package com.drp.common.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES 加密工具类
 * <p>
 * 使用 AES/GCM/NoPadding 模式，提供更好的安全性
 *
 * @author Nick
 */
public class AesEncryptUtil {

    /**
     * AES 算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * GCM 认证标签长度（128位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM IV 长度（96位 = 12字节）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * 默认密钥（Base64编码的32字节密钥，即256位）
     */
    private static final String DEFAULT_KEY = "dHJ1c3RlZF9rZXlfZm9yX2RycF9wbGF0Zm9ybV9rZXk=";

    /**
     * 私有构造函数，防止实例化
     */
    private AesEncryptUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // ==================== 加密/解密方法 ====================

    /**
     * 加密（使用默认密钥）
     *
     * @param content 明文内容
     * @return Base64编码的密文
     */
    public static String encrypt(String content) {
        return encrypt(content, DEFAULT_KEY);
    }

    /**
     * 解密（使用默认密钥）
     *
     * @param encrypted Base64编码的密文
     * @return 明文内容
     */
    public static String decrypt(String encrypted) {
        return decrypt(encrypted, DEFAULT_KEY);
    }

    /**
     * 加密
     *
     * @param content   明文内容
     * @param secretKey Base64编码的密钥（32字节 = 256位）
     * @return Base64编码的密文（包含IV）
     */
    public static String encrypt(String content, String secretKey) {
        try {
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // 创建密钥
            SecretKeySpec keySpec = new SecretKeySpec(
                    Base64.getDecoder().decode(secretKey), ALGORITHM);

            // 创建GCM参数
            GCMParameterSpec gcmSpec = new GCMParameterSpec(
                    GCM_TAG_LENGTH, iv);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            // 加密
            byte[] encryptedBytes = cipher.doFinal(
                    content.getBytes(StandardCharsets.UTF_8));

            // 合并IV和密文
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length,
                    encryptedBytes.length);

            // 返回Base64编码
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密
     *
     * @param encrypted Base64编码的密文（包含IV）
     * @param secretKey Base64编码的密钥（32字节 = 256位）
     * @return 明文内容
     */
    public static String decrypt(String encrypted, String secretKey) {
        try {
            // 解码Base64
            byte[] combined = Base64.getDecoder().decode(encrypted);

            // 提取IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            // 提取密文
            byte[] cipherText = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, cipherText, 0,
                    cipherText.length);

            // 创建密钥
            SecretKeySpec keySpec = new SecretKeySpec(
                    Base64.getDecoder().decode(secretKey), ALGORITHM);

            // 创建GCM参数
            GCMParameterSpec gcmSpec = new GCMParameterSpec(
                    GCM_TAG_LENGTH, iv);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            // 解密
            byte[] decryptedBytes = cipher.doFinal(cipherText);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }

    // ==================== 密钥生成方法 ====================

    /**
     * 生成随机密钥
     *
     * @return Base64编码的32字节随机密钥
     */
    public static String generateKey() {
        byte[] key = new byte[32]; // 256位
        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 验证密钥格式是否正确
     *
     * @param key Base64编码的密钥
     * @return 是否有效
     */
    public static boolean isValidKey(String key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(key);
            return decoded.length == 32;
        } catch (Exception e) {
            return false;
        }
    }
}
