package com.moneylog_backend.global.file;

import com.moneylog_backend.global.constant.ErrorMessageConstants;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {
    private static final int FILE_DELETE_RETRY_ATTEMPTS = 3;

    private final FileProperties fileProperties;
    private final LocalFileStorage localFileStorage;
    private final Optional<S3FileStorage> s3FileStorage;

    public String storeFile(MultipartFile multipartFile, String dirHint) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        validateUploadFile(multipartFile);
        return getActiveStorage().store(multipartFile, dirHint);
    }

    public FileUploadResult uploadFile(MultipartFile multipartFile, String dirHint) throws IOException {
        String fileUrl = storeFile(multipartFile, dirHint);
        return new FileUploadResult(
            fileUrl,
            multipartFile.getOriginalFilename(),
            multipartFile.getContentType(),
            multipartFile.getSize()
        );
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        resolveStorage(fileUrl).delete(fileUrl);
    }

    public FileDownloadResult downloadFile(String fileUrl, String originalName) {
        return resolveStorage(fileUrl).resolveDownload(fileUrl, originalName);
    }

    public String updateFile(String oldFileUrl, MultipartFile newFile, String dirHint) throws IOException {
        String newFileUrl = storeFile(newFile, dirHint);

        if (oldFileUrl == null || oldFileUrl.isBlank() || oldFileUrl.equals(newFileUrl)) {
            return newFileUrl;
        }

        deleteFileWithRetry(oldFileUrl);
        return newFileUrl;
    }

    private void deleteFileWithRetry(String fileUrl) {
        RuntimeException lastException = null;

        for (int attempt = 1; attempt <= FILE_DELETE_RETRY_ATTEMPTS; attempt++) {
            try {
                deleteFile(fileUrl);
                return;
            } catch (RuntimeException ex) {
                lastException = ex;
                log.warn("파일 삭제 재시도 실패. fileUrl={}, attempt={}/{}", fileUrl, attempt, FILE_DELETE_RETRY_ATTEMPTS, ex);
            }
        }

        log.warn("파일 삭제 재시도 종료. fileUrl={}, attempts={}", fileUrl, FILE_DELETE_RETRY_ATTEMPTS, lastException);
    }

    private FileStorage resolveStorage(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
        }

        if (fileUrl.startsWith("s3://")) {
            return s3FileStorage.orElseThrow(() -> new IllegalStateException(ErrorMessageConstants.S3_STORAGE_NOT_ENABLED));
        }

        if (localFileStorage.supports(fileUrl)) {
            return localFileStorage;
        }

        throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
    }

    private FileStorage getActiveStorage() {
        FileStorageType storageType = fileProperties.getStorage().getType();

        if (storageType == FileStorageType.S3) {
            return s3FileStorage.orElseThrow(() -> new IllegalStateException(ErrorMessageConstants.S3_STORAGE_NOT_ENABLED));
        }
        return localFileStorage;
    }

    private void validateUploadFile(MultipartFile multipartFile) {
        long maxSizeBytes = fileProperties.getMaxSizeBytes();
        if (maxSizeBytes > 0 && multipartFile.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_SIZE_EXCEEDED);
        }

        Set<String> allowedExtensions = fileProperties.getAllowedExtensions()
                                                      .stream()
                                                      .filter(ext -> ext != null && !ext.isBlank())
                                                      .map(ext -> ext.toLowerCase(Locale.ROOT))
                                                      .collect(Collectors.toSet());
        if (!allowedExtensions.isEmpty()) {
            String extension = extractExt(multipartFile.getOriginalFilename());
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException(ErrorMessageConstants.FILE_EXTENSION_NOT_ALLOWED);
            }
        }

        Set<String> allowedMimeTypes = fileProperties.getAllowedMimeTypes()
                                                     .stream()
                                                     .filter(contentType -> contentType != null && !contentType.isBlank())
                                                     .map(this::normalizeContentType)
                                                     .collect(Collectors.toSet());
        if (allowedMimeTypes.isEmpty()) {
            return;
        }

        String contentType = normalizeContentType(multipartFile.getContentType());
        if (contentType.isBlank()) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_CONTENT_TYPE_REQUIRED);
        }

        if (!allowedMimeTypes.contains(contentType)) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_CONTENT_TYPE_NOT_ALLOWED);
        }
    }

    private String extractExt(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_EXTENSION_REQUIRED);
        }

        int position = originalFilename.lastIndexOf('.');
        if (position == originalFilename.length() - 1) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_EXTENSION_REQUIRED);
        }
        return originalFilename.substring(position + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }

        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.indexOf(';');
        if (separator >= 0) {
            normalized = normalized.substring(0, separator).trim();
        }
        return normalized;
    }
}
