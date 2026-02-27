package com.moneylog_backend.global.file;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void 신루트에_파일이_있으면_정상_조회한다() throws IOException {
        Path primaryRoot = tempDir.resolve("primary");
        Path filePath = primaryRoot.resolve("profile/2026/02/27/a.jpg");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "primary", StandardCharsets.UTF_8);

        LocalFileStorage localFileStorage = new LocalFileStorage(buildProperties(primaryRoot, tempDir.resolve("legacy")));

        FileDownloadResult result = localFileStorage.resolveDownload("/uploads/profile/2026/02/27/a.jpg", null);

        Resource resource = result.getResource();
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertArrayEquals("primary".getBytes(StandardCharsets.UTF_8), resource.getInputStream().readAllBytes());
    }

    @Test
    void 신루트에_없고_구루트에_있으면_자동복사후_조회한다() throws IOException {
        Path primaryRoot = tempDir.resolve("primary");
        Path legacyRoot = tempDir.resolve("legacy");
        Path legacyFilePath = legacyRoot.resolve("profile/2026/02/27/a.jpg");
        Files.createDirectories(legacyFilePath.getParent());
        Files.writeString(legacyFilePath, "legacy", StandardCharsets.UTF_8);

        LocalFileStorage localFileStorage = new LocalFileStorage(buildProperties(primaryRoot, legacyRoot));

        FileDownloadResult result = localFileStorage.resolveDownload("/uploads/profile/2026/02/27/a.jpg", null);

        Path copiedPath = primaryRoot.resolve("profile/2026/02/27/a.jpg");
        assertTrue(Files.exists(copiedPath));
        assertEquals("legacy", Files.readString(copiedPath, StandardCharsets.UTF_8));
        assertArrayEquals("legacy".getBytes(StandardCharsets.UTF_8), result.getResource().getInputStream().readAllBytes());
    }

    @Test
    void 자동복사_실패해도_구루트_파일로_조회한다() throws IOException {
        Path legacyRoot = tempDir.resolve("legacy");
        Path legacyFilePath = legacyRoot.resolve("profile/2026/02/27/a.jpg");
        Files.createDirectories(legacyFilePath.getParent());
        Files.writeString(legacyFilePath, "legacy", StandardCharsets.UTF_8);

        Path brokenPrimaryRoot = tempDir.resolve("primary-file");
        Files.writeString(brokenPrimaryRoot, "not-directory", StandardCharsets.UTF_8);

        LocalFileStorage localFileStorage = new LocalFileStorage(buildProperties(brokenPrimaryRoot, legacyRoot));

        FileDownloadResult result = localFileStorage.resolveDownload("/uploads/profile/2026/02/27/a.jpg", null);

        assertArrayEquals("legacy".getBytes(StandardCharsets.UTF_8), result.getResource().getInputStream().readAllBytes());
    }

    @Test
    void 삭제시_신구루트_파일을_모두_삭제한다() throws IOException {
        Path primaryRoot = tempDir.resolve("primary");
        Path legacyRoot = tempDir.resolve("legacy");
        Path primaryFile = primaryRoot.resolve("profile/2026/02/27/a.jpg");
        Path legacyFile = legacyRoot.resolve("profile/2026/02/27/a.jpg");

        Files.createDirectories(primaryFile.getParent());
        Files.createDirectories(legacyFile.getParent());
        Files.writeString(primaryFile, "primary", StandardCharsets.UTF_8);
        Files.writeString(legacyFile, "legacy", StandardCharsets.UTF_8);

        LocalFileStorage localFileStorage = new LocalFileStorage(buildProperties(primaryRoot, legacyRoot));

        localFileStorage.delete("/uploads/profile/2026/02/27/a.jpg");

        assertFalse(Files.exists(primaryFile));
        assertFalse(Files.exists(legacyFile));
    }

    @Test
    void 구루트_미설정이면_폴백없이_기존동작을_유지한다() {
        Path primaryRoot = tempDir.resolve("primary");
        FileProperties fileProperties = buildProperties(primaryRoot, tempDir.resolve("legacy"));
        fileProperties.getLocal().setLegacyRootPath("   ");

        LocalFileStorage localFileStorage = new LocalFileStorage(fileProperties);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                                                           () -> localFileStorage.resolveDownload(
                                                               "/uploads/profile/2026/02/27/not-found.jpg",
                                                               null));
        assertEquals(ErrorMessageConstants.FILE_NOT_FOUND, exception.getMessage());
    }

    private FileProperties buildProperties(Path primaryRoot, Path legacyRoot) {
        FileProperties fileProperties = new FileProperties();
        fileProperties.getLocal().setRootPath(primaryRoot.toString());
        fileProperties.getLocal().setLegacyRootPath(legacyRoot.toString());
        fileProperties.getLocal().setBaseUrl("/uploads");
        return fileProperties;
    }
}
