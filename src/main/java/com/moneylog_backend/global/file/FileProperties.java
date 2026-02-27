package com.moneylog_backend.global.file;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileProperties {
    private Storage storage = new Storage();
    private long maxSizeBytes = 10 * 1024 * 1024;
    private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "gif", "webp"));
    private List<String> allowedMimeTypes = new ArrayList<>(List.of("image/jpeg", "image/png", "image/gif", "image/webp"));
    private Local local = new Local();
    private S3 s3 = new S3();
    private Cleanup cleanup = new Cleanup();

    @Getter
    @Setter
    public static class Storage {
        private FileStorageType type = FileStorageType.LOCAL;
    }

    @Getter
    @Setter
    public static class Local {
        private String rootPath = System.getProperty("user.dir") + "/uploads";
        private String legacyRootPath = System.getProperty("user.dir") + "/build/resources/main/static/uploads";
        private String baseUrl = "/uploads";
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucket = "";
        private String region = "ap-northeast-2";
        private String keyPrefix = "uploads";
        private long presignExpirationSeconds = 300;
    }

    @Getter
    @Setter
    public static class Cleanup {
        private boolean enabled = true;
        private long fixedDelayMs = 60000L;
        private int batchSize = 50;
        private int maxRetries = 20;
        private long retryBackoffSeconds = 300L;
    }
}
