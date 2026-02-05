package com.moneylog_backend.global.log.dto;

import com.moneylog_backend.global.log.entity.LogEntity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogDto {
    private String trace_id;
    private String service_name;
    private String method_name;
    private String request_params;
    private String result;
    private long execution_time;
    private boolean isSuccess;

    public LogEntity toEntity () {
        return LogEntity.builder()
                        .trace_id(this.trace_id)
                        .service_name(this.service_name)
                        .method_name(this.method_name)
                        .request_params(this.request_params)
                        .result(this.result)
                        .execution_time(this.execution_time)
                        .status(this.isSuccess ? "SUCCESS" : "FAIL")
                        .build();
    }
}
