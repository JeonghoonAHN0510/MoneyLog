package com.moneylog_backend.global.file.cleanup;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FileDeleteTaskRepository extends JpaRepository<FileDeleteTaskEntity, Long> {
    List<FileDeleteTaskEntity> findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
        FileDeleteTaskStatus status,
        LocalDateTime now,
        Pageable pageable
    );
}
