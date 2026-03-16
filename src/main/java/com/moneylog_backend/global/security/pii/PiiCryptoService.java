package com.moneylog_backend.global.security.pii;

import com.moneylog_backend.global.security.AppSecurityProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Service
public class PiiCryptoService {
    private static final String ENCRYPTION_PREFIX = "ENCv1:";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final String encryptionKeySource;
    private final String emailHashKeySource;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public PiiCryptoService(AppSecurityProperties appSecurityProperties) {
        this(
            appSecurityProperties.getPii().getEncryptionKey(),
            appSecurityProperties.getPii().getEmailHashKey()
        );
    }

    PiiCryptoService(String encryptionKeySource, String emailHashKeySource) {
        this.encryptionKeySource = encryptionKeySource;
        this.emailHashKeySource = emailHashKeySource;
    }

    public String encrypt(String value) {
        if (value == null) {
            return null;
        }
        if (value.isBlank() || isEncrypted(value)) {
            return value;
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(cipherText, 0, payload, iv.length, cipherText.length);

            return ENCRYPTION_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("민감정보 암호화에 실패했습니다.", ex);
        }
    }

    public String decrypt(String value) {
        if (value == null || value.isBlank() || !isEncrypted(value)) {
            return value;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(ENCRYPTION_PREFIX.length()));
            if (payload.length <= GCM_IV_LENGTH) {
                throw new IllegalStateException("암호문 형식이 올바르지 않습니다.");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[payload.length - GCM_IV_LENGTH];
            System.arraycopy(payload, 0, iv, 0, iv.length);
            System.arraycopy(payload, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("민감정보 복호화에 실패했습니다.", ex);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTION_PREFIX);
    }

    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public String hashEmail(String normalizedEmail) {
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return normalizedEmail;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(sha256(emailHashKeySource, "email-hash-key"), HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(normalizedEmail.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("이메일 해시 생성에 실패했습니다.", ex);
        }
    }

    private SecretKeySpec encryptionKey() {
        return new SecretKeySpec(sha256(encryptionKeySource, "encryption-key"), "AES");
    }

    private byte[] sha256(String source, String propertyName) {
        try {
            if (source == null || source.isBlank()) {
                throw new IllegalStateException("PII 보안 키가 설정되지 않았습니다: " + propertyName);
            }
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("보안 키 초기화에 실패했습니다.", ex);
        }
    }
}
