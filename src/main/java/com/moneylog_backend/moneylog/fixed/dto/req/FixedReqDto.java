package com.moneylog_backend.moneylog.fixed.dto.req;

import com.moneylog_backend.moneylog.fixed.entity.FixedEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

    private Integer accountId;

    @NotBlank(message = "지출명을 입력해주세요.")
    private String title;

    @NotNull(message = "금액은 필수입니다.")
    @PositiveOrZero(message = "금액은 0원 이상이어야 합니다.")
    private Integer amount;

    @NotNull(message = "고정 지출일은 필수입니다.")
    @Range(min = 1, max = 31, message = "지출일은 1일에서 31일 사이여야 합니다.")
    private Integer fixedDay;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public FixedEntity toEntity (Integer userId) {
        return FixedEntity.builder()
                          .userId(userId)
                          .categoryId(this.categoryId)
                          .accountId(this.accountId)
                          .title(this.title)
                          .amount(this.amount)
                          .fixedDay(this.fixedDay)
                          .startDate(this.startDate)
                          .endDate(this.endDate)
                          .build();
    }
}