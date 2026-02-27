package com.moneylog_backend.global.file.cleanup;

import com.moneylog_backend.global.common.BaseTime;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "file_delete_task")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileDeleteTaskEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id", columnDefinition = "BIGINT UNSIGNED")
    private Long taskId;

    @Column(name = "file_url", columnDefinition = "VARCHAR(1024) NOT NULL")
    private String fileUrl;

    @Column(name = "reason", columnDefinition = "VARCHAR(100)")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PENDING','FAILED') NOT NULL")
    private FileDeleteTaskStatus status;

    @Column(name = "retry_count", columnDefinition = "INT NOT NULL")
    private int retryCount;

    @Column(name = "next_retry_at", columnDefinition = "DATETIME(6) NOT NULL")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error", columnDefinition = "VARCHAR(1000)")
    private String lastError;

    public static FileDeleteTaskEntity pending(String fileUrl, String reason) {
        return FileDeleteTaskEntity.builder()
                                   .fileUrl(fileUrl)
                                   .reason(reason)
                                   .status(FileDeleteTaskStatus.PENDING)
                                   .retryCount(0)
                                   .nextRetryAt(LocalDateTime.now())
                                   .build();
    }

    public void markRetry(LocalDateTime nextRetryAt, String lastError) {
        this.retryCount += 1;
        this.nextRetryAt = nextRetryAt;
        this.lastError = truncate(lastError);
        this.status = FileDeleteTaskStatus.PENDING;
    }

    public void markFailed(String lastError) {
        this.retryCount += 1;
        this.status = FileDeleteTaskStatus.FAILED;
        this.lastError = truncate(lastError);
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }
}
