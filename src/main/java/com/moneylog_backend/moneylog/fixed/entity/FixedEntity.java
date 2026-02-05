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
    private int fixedId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int userId;
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int categoryId;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL")
    private String title;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(name = "fixed_day", columnDefinition = "INT NOT NULL")
    private int fixedDay;
    @Column(name = "start_date", columnDefinition = "DATE NOT NULL")
    private LocalDate startDate;
    @Column(name = "end_date", columnDefinition = "DATE")
    private LocalDate endDate;

    public FixedResDto toDto () {
        return FixedResDto.builder()
                          .fixedId(this.fixedId)
                          .userId(this.userId)
                          .categoryId(this.categoryId)
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