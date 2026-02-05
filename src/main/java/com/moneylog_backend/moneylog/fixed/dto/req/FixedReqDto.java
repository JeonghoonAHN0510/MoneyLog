package com.moneylog_backend.moneylog.fixed.dto.req;

import com.moneylog_backend.moneylog.fixed.entity.FixedEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedReqDto {
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

    public FixedEntity toEntity () {
        return FixedEntity.builder()
                          .fixedId(this.fixedId)
                          .userId(this.userId)
                          .categoryId(this.categoryId)
                          .title(this.title)
                          .amount(this.amount)
                          .fixedDay(this.fixedDay)
                          .startDate(this.startDate)
                          .endDate(this.endDate)
                          .build();
    }
}