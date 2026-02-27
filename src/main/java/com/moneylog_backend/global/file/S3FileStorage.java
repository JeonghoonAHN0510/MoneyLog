package com.moneylog_backend.global.file;

import com.moneylog_backend.global.constant.ErrorMessageConstants;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.file.storage", name = "type", havingValue = "s3")
public class S3FileStorage implements FileStorage {
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileProperties fileProperties;

    @Override
    public boolean supports(String fileRef) {
        return fileRef != null && fileRef.startsWith("s3://");
    }

    @Override
    public String store(MultipartFile multipartFile, String dirHint) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String extension = extractExt(multipartFile.getOriginalFilename());
        String storeFileName = UUID.randomUUID() + "." + extension;
        String key = buildObjectKey(dirHint, storeFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                            .bucket(fileProperties.getS3().getBucket())
                                                            .key(key)
                                                            .contentType(multipartFile.getContentType())
                                                            .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

        return "s3://" + fileProperties.getS3().getBucket() + "/" + key;
    }

    @Override
    public void delete(String fileRef) {
        S3ObjectRef s3ObjectRef = parse(fileRef);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                                                                     .bucket(s3ObjectRef.bucket())
                                                                     .key(s3ObjectRef.key())
                                                                     .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    @Override
    public FileDownloadResult resolveDownload(String fileRef, String originalName) {
        S3ObjectRef s3ObjectRef = parse(fileRef);

        GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                                                                  .bucket(s3ObjectRef.bucket())
                                                                  .key(s3ObjectRef.key());
        if (originalName != null && !originalName.isBlank()) {
            requestBuilder.responseContentDisposition(toContentDisposition(originalName));
        }

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                                   .signatureDuration(Duration.ofSeconds(fileProperties.getS3()
                                                                                      .getPresignExpirationSeconds()))
                                   .getObjectRequest(requestBuilder.build())
                                   .build()
        );

        return FileDownloadResult.redirect(presignedRequest.url().toString());
    }

    private String buildObjectKey(String dirHint, String storeFileName) {
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        String normalizedDirHint = normalizeDirHint(dirHint);
        String normalizedKeyPrefix = normalizeKeyPrefix(fileProperties.getS3().getKeyPrefix());

        List<String> parts = new ArrayList<>();
        if (!normalizedKeyPrefix.isBlank()) {
            parts.add(normalizedKeyPrefix);
        }
        if (!normalizedDirHint.isBlank()) {
            parts.add(normalizedDirHint);
        }
        parts.add(datePath);
        parts.add(storeFileName);

        return String.join("/", parts);
    }

    private String normalizeKeyPrefix(String keyPrefix) {
        if (keyPrefix == null || keyPrefix.isBlank()) {
            return "";
        }

        String normalized = keyPrefix.trim().replace("\\", "/");
        normalized = normalized.replaceAll("^/+", "").replaceAll("/+$", "");

        if (normalized.contains("..")) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_UPLOAD_DIRECTORY);
        }
        return normalized;
    }

    private String normalizeDirHint(String dirHint) {
        if (dirHint == null || dirHint.isBlank()) {
            return "";
        }

        String normalized = dirHint.trim().replace("\\", "/");
        normalized = normalized.replaceAll("^/+", "").replaceAll("/+$", "");

        if (normalized.contains("..") || !normalized.matches("[a-zA-Z0-9/_-]+")) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_UPLOAD_DIRECTORY);
        }
        return normalized;
    }

    private String extractExt(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_EXTENSION_REQUIRED);
        }

        int position = originalFilename.lastIndexOf('.');
        if (position == originalFilename.length() - 1) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_EXTENSION_REQUIRED);
        }
        return originalFilename.substring(position + 1).toLowerCase();
    }

    private S3ObjectRef parse(String fileRef) {
        if (!supports(fileRef)) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
        }

        URI uri = URI.create(fileRef);
        String bucket = uri.getHost();
        String path = uri.getPath();

        if (bucket == null || bucket.isBlank() || path == null || path.length() <= 1) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
        }

        String expectedBucket = fileProperties.getS3().getBucket();
        if (expectedBucket == null || expectedBucket.isBlank() || !bucket.equals(expectedBucket)) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
        }

        String key = path.substring(1);
        String normalizedKeyPrefix = normalizeKeyPrefix(fileProperties.getS3().getKeyPrefix());
        if (!normalizedKeyPrefix.isBlank() && !isKeyWithinPrefix(key, normalizedKeyPrefix)) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
        }

        return new S3ObjectRef(bucket, key);
    }

    private boolean isKeyWithinPrefix(String key, String keyPrefix) {
        return key.equals(keyPrefix) || key.startsWith(keyPrefix + "/");
    }

    private String toContentDisposition(String originalName) {
        String encoded = java.net.URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encoded;
    }

    private record S3ObjectRef(String bucket, String key) {
    }
}
