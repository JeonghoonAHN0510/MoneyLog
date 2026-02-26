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

@Service
@RequiredArgsConstructor
public class FileStorageService {
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
        deleteFile(oldFileUrl);
        return storeFile(newFile, dirHint);
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
                                                      .map(ext -> ext.toLowerCase(Locale.ROOT))
                                                      .collect(Collectors.toSet());
        if (allowedExtensions.isEmpty()) {
            return;
        }

        String extension = extractExt(multipartFile.getOriginalFilename());
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException(ErrorMessageConstants.FILE_EXTENSION_NOT_ALLOWED);
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
}
