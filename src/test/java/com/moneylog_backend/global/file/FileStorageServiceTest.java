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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InOrder;

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
    void uploadFile에서_null_파일이면_업로드를_거부한다() throws IOException {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> fileStorageService.uploadFile(null, "profile"));
        assertEquals(ErrorMessageConstants.FILE_REQUIRED, ex.getMessage());
        verify(localFileStorage, never()).store(any(MultipartFile.class), any());
    }

    @Test
    void uploadFile에서_empty_파일이면_업로드를_거부한다() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> fileStorageService.uploadFile(file, "profile"));
        assertEquals(ErrorMessageConstants.FILE_REQUIRED, ex.getMessage());
        verify(localFileStorage, never()).store(any(MultipartFile.class), any());
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

    @Test
    void updateFile에서_새파일_저장_실패시_기존파일을_삭제하지_않는다() throws IOException {
        MultipartFile newFile = new MockMultipartFile("file", "new.jpg", "image/jpeg", "abc".getBytes(StandardCharsets.UTF_8));
        when(localFileStorage.store(eq(newFile), eq("profile"))).thenThrow(new IOException("store fail"));

        assertThrows(IOException.class, () -> fileStorageService.updateFile("/uploads/old.jpg", newFile, "profile"));
        verify(localFileStorage, never()).delete(any());
    }

    @Test
    void updateFile에서_새파일_저장성공후_기존파일_삭제를_시도한다() throws IOException {
        MultipartFile newFile = new MockMultipartFile("file", "new.jpg", "image/jpeg", "abc".getBytes(StandardCharsets.UTF_8));
        when(localFileStorage.store(eq(newFile), eq("profile"))).thenReturn("/uploads/new.jpg");
        when(localFileStorage.supports("/uploads/old.jpg")).thenReturn(true);

        String result = fileStorageService.updateFile("/uploads/old.jpg", newFile, "profile");

        assertEquals("/uploads/new.jpg", result);
        InOrder inOrder = inOrder(localFileStorage);
        inOrder.verify(localFileStorage).store(eq(newFile), eq("profile"));
        inOrder.verify(localFileStorage).delete("/uploads/old.jpg");
    }

    @Test
    void updateFile에서_기존파일_삭제실패시_재시도후_성공을_유지한다() throws IOException {
        MultipartFile newFile = new MockMultipartFile("file", "new.jpg", "image/jpeg", "abc".getBytes(StandardCharsets.UTF_8));
        when(localFileStorage.store(eq(newFile), eq("profile"))).thenReturn("/uploads/new.jpg");
        when(localFileStorage.supports("/uploads/old.jpg")).thenReturn(true);
        doThrow(new RuntimeException("delete fail")).when(localFileStorage).delete("/uploads/old.jpg");

        String result = fileStorageService.updateFile("/uploads/old.jpg", newFile, "profile");

        assertEquals("/uploads/new.jpg", result);
        verify(localFileStorage, times(3)).delete("/uploads/old.jpg");
    }
}
