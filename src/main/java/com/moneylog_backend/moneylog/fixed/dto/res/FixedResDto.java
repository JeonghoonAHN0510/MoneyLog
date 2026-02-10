package com.moneylog_backend.moneylog.fixed.dto.res;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FixedResDto {
    private Integer fixedId;
    private Integer userId;
    private Integer categoryId;
    private Integer accountId;
    private String title;
    private Integer amount;
    private Integer fixedDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
