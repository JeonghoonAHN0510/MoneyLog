package com.moneylog_backend.moneylog.fixed.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.fixed.dto.res.FixedResDto;

import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "fixed")
@Getter
@SuperBuilder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FixedEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fixed_id", columnDefinition = "INT UNSIGNED")
    private Integer fixedId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer categoryId;
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer accountId;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL")
    private String title;
    @Column(columnDefinition = "INT NOT NULL")
    private Integer amount;
    @Column(name = "fixed_day", columnDefinition = "INT NOT NULL")
    private Integer fixedDay;
    @Column(name = "start_date", columnDefinition = "DATE NOT NULL")
    private LocalDate startDate;
    @Column(name = "end_date", columnDefinition = "DATE")
    private LocalDate endDate;

    public FixedResDto toResDto () {
        return FixedResDto.builder()
                          .fixedId(this.fixedId)
                          .userId(this.userId)
                          .categoryId(this.categoryId)
                          .accountId(this.accountId)
                          .title(this.title)
                          .amount(this.amount)
                          .fixedDay(this.fixedDay)
                          .startDate(this.startDate)
                          .endDate(this.endDate)
                          .createdAt(this.getCreatedAt())
                          .updatedAt(this.getUpdatedAt())
                          .build();
    }
}