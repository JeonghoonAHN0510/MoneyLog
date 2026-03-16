package com.moneylog_backend.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {
    @Valid
    private Pii pii = new Pii();

    @Valid
    private RedisSecret redisSecret = new RedisSecret();

    @Getter
    @Setter
    public static class Pii {
        @NotBlank
        private String encryptionKey;

        @NotBlank
        private String emailHashKey;
    }

    @Getter
    @Setter
    public static class RedisSecret {
        @NotBlank
        private String hashKey;
    }
}
