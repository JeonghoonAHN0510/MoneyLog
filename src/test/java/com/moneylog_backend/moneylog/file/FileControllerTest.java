package com.moneylog_backend.moneylog.file;

import com.moneylog_backend.global.file.FileDownloadResult;
import com.moneylog_backend.global.file.FileStorageService;
import com.moneylog_backend.global.file.cleanup.DeleteDispatchResult;
import com.moneylog_backend.global.file.cleanup.FileDeleteTaskService;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileControllerTest {
    @Test
    void view_응답에_nosniff_헤더가_포함된다() {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileDeleteTaskService fileDeleteTaskService = mock(FileDeleteTaskService.class);
        FileController fileController = new FileController(fileStorageService, fileDeleteTaskService);

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

    @Test
    void delete_즉시삭제_성공이면_200을_반환한다() {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileDeleteTaskService fileDeleteTaskService = mock(FileDeleteTaskService.class);
        FileController fileController = new FileController(fileStorageService, fileDeleteTaskService);

        when(fileDeleteTaskService.deleteNowOrEnqueueWithResult("/uploads/a.jpg", "MANUAL_DELETE_REQUEST"))
            .thenReturn(DeleteDispatchResult.DELETED_NOW);

        ResponseEntity<String> response = fileController.delete("/uploads/a.jpg");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
    }

    @Test
    void delete_큐적재면_202를_반환한다() {
        FileStorageService fileStorageService = mock(FileStorageService.class);
        FileDeleteTaskService fileDeleteTaskService = mock(FileDeleteTaskService.class);
        FileController fileController = new FileController(fileStorageService, fileDeleteTaskService);

        when(fileDeleteTaskService.deleteNowOrEnqueueWithResult("/uploads/a.jpg", "MANUAL_DELETE_REQUEST"))
            .thenReturn(DeleteDispatchResult.ENQUEUED);

        ResponseEntity<String> response = fileController.delete("/uploads/a.jpg");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Queued for deletion", response.getBody());
    }
}
