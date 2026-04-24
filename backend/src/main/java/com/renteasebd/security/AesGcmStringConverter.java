package com.renteasebd.security;

import com.renteasebd.common.AppException;
import jakarta.persistence.AttributeConverter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.persistence.Converter;

@Converter
public class AesGcmStringConverter implements AttributeConverter<String, byte[]> {

    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private static volatile byte[] configuredKey;
    private byte[] key;
    private final SecureRandom random = new SecureRandom();

    public static void configureBase64Key(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            configuredKey = null;
            return;
        }
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        if (decoded.length != 32) {
            throw new AppException("INVALID_AES_KEY", "AES key must be 32 bytes (base64)", "app.crypto.aes256-key-base64");
        }
        configuredKey = decoded;
    }

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        byte[] resolvedKey = resolveKey();
        try {
            byte[] iv = new byte[IV_SIZE];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(resolvedKey, "AES"), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return buffer.array();
        } catch (Exception ex) {
            throw new AppException("ENCRYPTION_ERROR", "Failed to encrypt field", null);
        }
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        byte[] resolvedKey = resolveKey();
        try {
            ByteBuffer buffer = ByteBuffer.wrap(dbData);
            byte[] iv = new byte[IV_SIZE];
            buffer.get(iv);
            byte[] cipherBytes = new byte[buffer.remaining()];
            buffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(resolvedKey, "AES"), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] decrypted = cipher.doFinal(cipherBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new AppException("DECRYPTION_ERROR", "Failed to decrypt field", null);
        }
    }

    private byte[] resolveKey() {
        if (key != null) {
            return key;
        }
        if (configuredKey != null) {
            key = configuredKey;
            return key;
        }
        String base64Key = System.getenv("PII_AES256_KEY_B64");
        if (base64Key == null || base64Key.isBlank()) {
            throw new AppException("INVALID_AES_KEY", "PII_AES256_KEY_B64 is not configured", "PII_AES256_KEY_B64");
        }
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        if (decoded.length != 32) {
            throw new AppException("INVALID_AES_KEY", "AES key must be 32 bytes (base64)", "PII_AES256_KEY_B64");
        }
        key = decoded;
        return key;
    }
}
