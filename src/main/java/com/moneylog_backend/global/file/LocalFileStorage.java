package com.moneylog_backend.global.file;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorage {
    private static final String LEGACY_BASE_URL = "/uploads/";
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileProperties fileProperties;

    @Override
    public boolean supports(String fileRef) {
        if (fileRef == null || fileRef.isBlank() || fileRef.startsWith("s3://")) {
            return false;
        }

        String baseUrlPrefix = normalizeBaseUrl(fileProperties.getLocal().getBaseUrl()) + "/";
        return fileRef.startsWith(baseUrlPrefix) || fileRef.startsWith(LEGACY_BASE_URL);
    }

    @Override
    public String store(MultipartFile multipartFile, String dirHint) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String extension = extractExt(originalFilename);
        String storeFileName = UUID.randomUUID() + "." + extension;

        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        String normalizedDirHint = normalizeDirHint(dirHint);
        String relativeDirectory = normalizedDirHint.isEmpty() ? datePath : normalizedDirHint + "/" + datePath;

        Path rootPath = getPrimaryRootPath();
        Path saveDirectory = resolveSafePath(rootPath, relativeDirectory);
        Files.createDirectories(saveDirectory);

        Path targetPath = saveDirectory.resolve(storeFileName).normalize();
        if (!targetPath.startsWith(rootPath)) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
        }

        multipartFile.transferTo(targetPath.toFile());

        return normalizeBaseUrl(fileProperties.getLocal().getBaseUrl()) + "/" + relativeDirectory + "/" + storeFileName;
    }

    @Override
    public void delete(String fileRef) {
        if (fileRef == null || fileRef.isBlank()) {
            return;
        }

        String relativePath = extractRelativePath(fileRef);
        Path primaryPath = resolveSafePath(getPrimaryRootPath(), relativePath);
        Optional<Path> legacyPath = getLegacyRootPath().map(rootPath -> resolveSafePath(rootPath, relativePath));

        try {
            Files.deleteIfExists(primaryPath);
            if (legacyPath.isPresent() && !legacyPath.get().equals(primaryPath)) {
                Files.deleteIfExists(legacyPath.get());
            }
        } catch (IOException e) {
            throw new IllegalStateException(ErrorMessageConstants.FILE_DELETE_FAILED, e);
        }
    }

    @Override
    public FileDownloadResult resolveDownload(String fileRef, String originalName) {
        String relativePath = extractRelativePath(fileRef);
        Path primaryPath = resolveSafePath(getPrimaryRootPath(), relativePath);

        Path resolvedPath = primaryPath;
        if (!Files.exists(primaryPath)) {
            Path legacyPath = getLegacyRootPath().map(rootPath -> resolveSafePath(rootPath, relativePath)).orElse(null);
            if (legacyPath == null || !Files.exists(legacyPath)) {
                throw new ResourceNotFoundException(ErrorMessageConstants.FILE_NOT_FOUND);
            }

            resolvedPath = legacyPath;
            Path copiedPath = copyLegacyFileToPrimaryIfPossible(legacyPath, primaryPath);
            if (copiedPath != null) {
                resolvedPath = copiedPath;
            }
        }

        try {
            Resource resource = new UrlResource(resolvedPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException(ErrorMessageConstants.FILE_NOT_FOUND);
            }
            return FileDownloadResult.local(resource);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL, e);
        }
    }

    private Path copyLegacyFileToPrimaryIfPossible(Path legacyPath, Path primaryPath) {
        try {
            Path primaryParent = primaryPath.getParent();
            if (primaryParent != null) {
                Files.createDirectories(primaryParent);
            }
            Files.copy(legacyPath, primaryPath, StandardCopyOption.REPLACE_EXISTING);
            return primaryPath;
        } catch (IOException ex) {
            log.warn("레거시 파일 자동 복사 실패. legacyPath={}, primaryPath={}", legacyPath, primaryPath, ex);
            return null;
        }
    }

    private String extractRelativePath(String fileRef) {
        String baseUrlPrefix = normalizeBaseUrl(fileProperties.getLocal().getBaseUrl()) + "/";

        if (fileRef.startsWith(baseUrlPrefix)) {
            return fileRef.substring(baseUrlPrefix.length());
        }
        if (fileRef.startsWith(LEGACY_BASE_URL)) {
            return fileRef.substring(LEGACY_BASE_URL.length());
        }
        throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
    }

    private Path resolveSafePath(Path rootPath, String relativePath) {
        Path resolvedPath = rootPath.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(rootPath)) {
            throw new IllegalArgumentException(ErrorMessageConstants.INVALID_FILE_URL);
        }
        return resolvedPath;
    }

    private Path getPrimaryRootPath() {
        return Paths.get(fileProperties.getLocal().getRootPath()).toAbsolutePath().normalize();
    }

    private Optional<Path> getLegacyRootPath() {
        String legacyRootPath = fileProperties.getLocal().getLegacyRootPath();
        if (legacyRootPath == null || legacyRootPath.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Paths.get(legacyRootPath).toAbsolutePath().normalize());
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

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "/uploads";
        }

        String normalized = baseUrl.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
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
}
