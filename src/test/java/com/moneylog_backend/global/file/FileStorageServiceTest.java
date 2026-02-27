package com.moneylog_backend.global.file;

import com.moneylog_backend.global.constant.ErrorMessageConstants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileStorageServiceTest {
    private FileStorageService fileStorageService;
    private LocalFileStorage localFileStorage;

    @BeforeEach
    void setUp() throws IOException {
        FileProperties fileProperties = new FileProperties();
        fileProperties.getStorage().setType(FileStorageType.LOCAL);

        localFileStorage = mock(LocalFileStorage.class);
        when(localFileStorage.store(any(MultipartFile.class), any())).thenReturn("/uploads/profile/test.jpg");

        fileStorageService = new FileStorageService(fileProperties, localFileStorage, Optional.empty());
    }

    @Test
    void 허용_확장자와_MIME이면_업로드를_통과한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpg",
            "image/jpeg",
            "abc".getBytes(StandardCharsets.UTF_8)
        );

        String result = fileStorageService.storeFile(file, "profile");

        assertEquals("/uploads/profile/test.jpg", result);
        verify(localFileStorage).store(eq(file), eq("profile"));
    }

    @Test
    void MIME_타입이_허용목록에_없으면_업로드를_거부한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpg",
            "text/html",
            "<html></html>".getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> fileStorageService.storeFile(file, "profile"));
        assertEquals(ErrorMessageConstants.FILE_CONTENT_TYPE_NOT_ALLOWED, ex.getMessage());
        verify(localFileStorage, never()).store(any(MultipartFile.class), any());
    }

    @Test
    void MIME_타입이_없으면_업로드를_거부한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpg",
            null,
            "abc".getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> fileStorageService.storeFile(file, "profile"));
        assertEquals(ErrorMessageConstants.FILE_CONTENT_TYPE_REQUIRED, ex.getMessage());
        verify(localFileStorage, never()).store(any(MultipartFile.class), any());
    }

    @Test
    void MIME_파라미터가_포함돼도_기본_MIME이_허용되면_통과한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpeg",
            "image/jpeg; charset=UTF-8",
            "abc".getBytes(StandardCharsets.UTF_8)
        );

        String result = fileStorageService.storeFile(file, "profile");

        assertEquals("/uploads/profile/test.jpg", result);
        verify(localFileStorage).store(eq(file), eq("profile"));
    }

    @Test
    void 확장자가_허용목록에_없으면_업로드를_거부한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.txt",
            "image/jpeg",
            "abc".getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> fileStorageService.storeFile(file, "profile"));
        assertEquals(ErrorMessageConstants.FILE_EXTENSION_NOT_ALLOWED, ex.getMessage());
        verify(localFileStorage, never()).store(any(MultipartFile.class), any());
    }

    @Test
    void 파일_크기가_최대_허용치를_초과하면_업로드를_거부한다() throws IOException {
        FileProperties fileProperties = new FileProperties();
        fileProperties.setMaxSizeBytes(2);

        FileStorageService sizeLimitedService = new FileStorageService(fileProperties, localFileStorage, Optional.empty());
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profile.jpg",
            "image/jpeg",
            "12345".getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> sizeLimitedService.storeFile(file, "profile"));
        assertEquals(ErrorMessageConstants.FILE_SIZE_EXCEEDED, ex.getMessage());
        verify(localFileStorage, never()).store(any(MultipartFile.class), any());
    }
}
