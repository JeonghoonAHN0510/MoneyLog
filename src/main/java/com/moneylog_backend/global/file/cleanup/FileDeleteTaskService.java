package com.moneylog_backend.global.file.cleanup;

import com.moneylog_backend.global.file.FileProperties;
import com.moneylog_backend.global.file.FileStorageService;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileDeleteTaskService {
    private final FileDeleteTaskRepository fileDeleteTaskRepository;
    private final FileStorageService fileStorageService;
    private final FileProperties fileProperties;

    @Transactional
    public void enqueueDelete(String fileUrl, String reason) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        fileDeleteTaskRepository.save(FileDeleteTaskEntity.pending(fileUrl, reason));
    }

    @Transactional
    public void deleteNowOrEnqueue(String fileUrl, String reason) {
        deleteNowOrEnqueueWithResult(fileUrl, reason);
    }

    @Transactional
    public DeleteDispatchResult deleteNowOrEnqueueWithResult(String fileUrl, String reason) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return DeleteDispatchResult.DELETED_NOW;
        }

        try {
            fileStorageService.deleteFile(fileUrl);
            return DeleteDispatchResult.DELETED_NOW;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("즉시 파일 삭제 실패. 큐에 적재합니다. fileUrl={}, reason={}", fileUrl, reason, ex);
            enqueueDelete(fileUrl, reason);
            return DeleteDispatchResult.ENQUEUED;
        }
    }

    @Transactional
    public void processPendingTasks() {
        FileProperties.Cleanup cleanup = fileProperties.getCleanup();
        List<FileDeleteTaskEntity> pendingTasks = fileDeleteTaskRepository.findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            FileDeleteTaskStatus.PENDING,
            LocalDateTime.now(),
            PageRequest.of(0, cleanup.getBatchSize())
        );

        for (FileDeleteTaskEntity task : pendingTasks) {
            processTask(task, cleanup);
        }
    }

    private void processTask(FileDeleteTaskEntity task, FileProperties.Cleanup cleanup) {
        try {
            fileStorageService.deleteFile(task.getFileUrl());
            fileDeleteTaskRepository.delete(task);
        } catch (RuntimeException ex) {
            int nextRetryCount = task.getRetryCount() + 1;
            if (nextRetryCount >= cleanup.getMaxRetries()) {
                task.markFailed(ex.getMessage());
                log.warn("파일 삭제 재처리 최종 실패. taskId={}, fileUrl={}", task.getTaskId(), task.getFileUrl(), ex);
                return;
            }

            LocalDateTime nextRetryAt = LocalDateTime.now().plusSeconds(cleanup.getRetryBackoffSeconds());
            task.markRetry(nextRetryAt, ex.getMessage());
            log.warn("파일 삭제 재처리 실패. taskId={}, retry={}/{}, nextRetryAt={}",
                     task.getTaskId(),
                     nextRetryCount,
                     cleanup.getMaxRetries(),
                     nextRetryAt,
                     ex);
        }
    }
}
