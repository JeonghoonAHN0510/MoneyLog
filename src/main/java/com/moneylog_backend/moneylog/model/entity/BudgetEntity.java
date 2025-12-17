package com.moneylog_backend.moneylog.model.entity;

import com.moneylog_backend.moneylog.model.dto.BudgetDto;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budget", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_budget_user_date_category",
                columnNames = {"user_id", "budget_date", "category_id"}
        )
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetEntity extends BaseTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int budget_id;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate budget_date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CategoryEntity categoryEntity;

    public BudgetDto toDto(){
        return BudgetDto.builder()
                .budget_id(this.budget_id)
                .amount(this.amount)
                .budget_date(this.budget_date)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .category_id(this.categoryEntity != null ? this.categoryEntity.getCategory_id() : 0)
                .created_at(this.getCreated_at())
                .updated_at(this.getUpdated_at())
                .build();
    } // func end
} // class end