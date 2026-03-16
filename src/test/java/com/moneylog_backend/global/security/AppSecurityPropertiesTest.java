package com.moneylog_backend.global.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppSecurityPropertiesTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 필수_보안_키가_비어있으면_검증에_실패한다() {
        AppSecurityProperties properties = new AppSecurityProperties();
        properties.getPii().setEncryptionKey("");
        properties.getPii().setEmailHashKey("");
        properties.getRedisSecret().setHashKey("");

        Set<ConstraintViolation<AppSecurityProperties>> violations = validator.validate(properties);

        assertEquals(3, violations.size());
        assertTrue(violations.stream().anyMatch(v -> "pii.encryptionKey".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "pii.emailHashKey".equals(v.getPropertyPath().toString())));
        assertTrue(violations.stream().anyMatch(v -> "redisSecret.hashKey".equals(v.getPropertyPath().toString())));
    }

    @Test
    void 필수_보안_키가_채워지면_검증을_통과한다() {
        AppSecurityProperties properties = new AppSecurityProperties();
        properties.getPii().setEncryptionKey("enc-key");
        properties.getPii().setEmailHashKey("email-hash-key");
        properties.getRedisSecret().setHashKey("redis-secret-key");

        Set<ConstraintViolation<AppSecurityProperties>> violations = validator.validate(properties);

        assertTrue(violations.isEmpty());
    }
}
