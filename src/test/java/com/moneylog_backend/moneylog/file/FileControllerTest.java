package com.moneylog_backend.moneylog.file;

import com.moneylog_backend.global.file.FileDownloadResult;
import com.moneylog_backend.global.file.FileStorageService;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileControllerTest {
    @Test
    void view_응답에_nosniff_헤더가_포함된다() {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileController fileController = new FileController(fileStorageService);

        Resource resource = new ByteArrayResource("image".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "profile.png";
            }
        };

        when(fileStorageService.downloadFile("/uploads/profile.png", null)).thenReturn(FileDownloadResult.local(resource));

        ResponseEntity<?> response = fileController.view("/uploads/profile.png");

        assertEquals("inline", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
    }
}
