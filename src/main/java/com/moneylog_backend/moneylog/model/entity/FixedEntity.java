package com.moneylog_backend.moneylog.model.entity;

import com.moneylog_backend.moneylog.model.dto.FixedDto;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fixed")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FixedEntity extends BaseTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int fixed_id;
    @Column(columnDefinition = "VARCHAR(100) NOT NULL")
    private String title;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(columnDefinition = "INT NOT NULL")
    private int fixed_day;
    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate start_date;
    @Column(columnDefinition = "DATE")
    private LocalDate end_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CategoryEntity categoryEntity;

    public FixedDto toDto(){
        return FixedDto.builder()
                .fixed_id(this.fixed_id)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .category_id(this.categoryEntity != null ? this.categoryEntity.getCategory_id() : 0)
                .title(this.title)
                .amount(this.amount)
                .fixed_day(this.fixed_day)
                .start_date(this.start_date)
                .end_date(this.end_date)
                .created_at(this.getCreated_at())
                .updated_at(this.getUpdated_at())
                .build();
    } // func end
} // class end