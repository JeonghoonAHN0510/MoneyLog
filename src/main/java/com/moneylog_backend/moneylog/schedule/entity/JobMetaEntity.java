package com.moneylog_backend.moneylog.schedule.entity;

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
    private String job_name;        // 예: "logCleanupJob"
    private String job_group;       // 예: "system"
    private String cron_expression; // 예: "0 0 3 * * ?"
    private String description;     // 예: "30일 지난 로그 삭제"
    private boolean is_active;      // 활성화 여부
}
