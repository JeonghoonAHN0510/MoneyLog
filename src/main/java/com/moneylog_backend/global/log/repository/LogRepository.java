package com.moneylog_backend.global.log.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.moneylog_backend.global.log.entity.LogEntity;

public interface LogRepository extends JpaRepository<LogEntity, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM LogEntity s WHERE s.created_at < :cutoffDate")
    void deleteLogsOlderThan (@Param("cutoffDate") LocalDateTime cutoffDate);
}
