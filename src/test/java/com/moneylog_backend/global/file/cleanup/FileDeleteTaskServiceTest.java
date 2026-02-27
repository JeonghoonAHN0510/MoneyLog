package com.moneylog_backend.global.file.cleanup;

import com.moneylog_backend.global.file.FileProperties;
import com.moneylog_backend.global.file.FileStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDeleteTaskServiceTest {
    @Mock
    private FileDeleteTaskRepository fileDeleteTaskRepository;
    @Mock
    private FileStorageService fileStorageService;

    private FileProperties fileProperties;

    @InjectMocks
    private FileDeleteTaskService fileDeleteTaskService;

    @BeforeEach
    void setUp() {
        fileProperties = new FileProperties();
        fileProperties.getCleanup().setBatchSize(10);
        fileProperties.getCleanup().setMaxRetries(3);
        fileProperties.getCleanup().setRetryBackoffSeconds(60);
        fileDeleteTaskService = new FileDeleteTaskService(fileDeleteTaskRepository, fileStorageService, fileProperties);
    }

    @Test
    void 즉시삭제_실패시_삭제큐에_적재한다() {
        doThrow(new RuntimeException("delete fail")).when(fileStorageService).deleteFile("/uploads/a.jpg");

        DeleteDispatchResult result = fileDeleteTaskService.deleteNowOrEnqueueWithResult("/uploads/a.jpg", "TEST");

        assertEquals(DeleteDispatchResult.ENQUEUED, result);
        verify(fileDeleteTaskRepository).save(any(FileDeleteTaskEntity.class));
    }

    @Test
    void 즉시삭제_성공시_DELETED_NOW를_반환한다() {
        DeleteDispatchResult result = fileDeleteTaskService.deleteNowOrEnqueueWithResult("/uploads/a.jpg", "TEST");

        assertEquals(DeleteDispatchResult.DELETED_NOW, result);
        verify(fileStorageService).deleteFile("/uploads/a.jpg");
        verify(fileDeleteTaskRepository, never()).save(any(FileDeleteTaskEntity.class));
    }

    @Test
    void 재처리_성공시_작업을_삭제한다() {
        FileDeleteTaskEntity task = FileDeleteTaskEntity.pending("/uploads/a.jpg", "TEST");
        when(fileDeleteTaskRepository.findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            eq(FileDeleteTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of(task));

        fileDeleteTaskService.processPendingTasks();

        verify(fileStorageService).deleteFile("/uploads/a.jpg");
        verify(fileDeleteTaskRepository).delete(task);
    }

    @Test
    void 재처리_실패시_재시도_정보를_갱신한다() {
        FileDeleteTaskEntity task = FileDeleteTaskEntity.pending("/uploads/a.jpg", "TEST");
        when(fileDeleteTaskRepository.findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            eq(FileDeleteTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of(task));
        doThrow(new RuntimeException("delete fail")).when(fileStorageService).deleteFile("/uploads/a.jpg");

        fileDeleteTaskService.processPendingTasks();

        assertEquals(1, task.getRetryCount());
        assertEquals(FileDeleteTaskStatus.PENDING, task.getStatus());
        assertNotNull(task.getNextRetryAt());
        verify(fileDeleteTaskRepository, never()).delete(task);
    }

    @Test
    void 최대재시도_도달시_FAILED로_전환한다() {
        FileDeleteTaskEntity task = FileDeleteTaskEntity.pending("/uploads/a.jpg", "TEST");
        task.markRetry(LocalDateTime.now().minusMinutes(1), "first fail");
        task.markRetry(LocalDateTime.now().minusMinutes(1), "second fail");

        when(fileDeleteTaskRepository.findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            eq(FileDeleteTaskStatus.PENDING), any(LocalDateTime.class), any(Pageable.class)
        )).thenReturn(List.of(task));
        doThrow(new RuntimeException("delete fail")).when(fileStorageService).deleteFile("/uploads/a.jpg");

        fileDeleteTaskService.processPendingTasks();

        assertEquals(3, task.getRetryCount());
        assertEquals(FileDeleteTaskStatus.FAILED, task.getStatus());
        verify(fileDeleteTaskRepository, never()).delete(task);
    }
}
