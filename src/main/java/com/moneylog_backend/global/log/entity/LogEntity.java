package com.moneylog_backend.global.log.entity;

import com.moneylog_backend.global.common.BaseTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "system_logs", indexes = @Index(name = "idx_trace_id", columnList = "traceId"))
public class LogEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trace_id;
    private String service_name;
    private String method_name;
    @Column(columnDefinition = "TEXT")
    private String request_params;

    @Column(columnDefinition = "TEXT")
    private String result;

    private Long execution_time;
    private String status;
}
