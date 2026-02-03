package com.moneylog_backend.moneylog.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMetaEntity {
    @Id
    @Column(name = "job_name")
    private String jobName;        // 예: "logCleanupJob"
    @Column(name = "job_group")
    private String jobGroup;       // 예: "system"
    @Column(name = "cron_expression")
    private String cronExpression; // 예: "0 0 3 * * ?"
    @Column(name = "description")
    private String description;     // 예: "30일 지난 로그 삭제"
    @Column(name = "is_active")
    private boolean isActive;      // 활성화 여부
}
