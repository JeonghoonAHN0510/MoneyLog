package com.moneylog_backend.moneylog.fixed.dto.res;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class FixedResDto {
    private Integer fixedId;
    private Integer userId;
    private Integer categoryId;
    private String title;
    private Integer amount;
    private Integer fixedDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
