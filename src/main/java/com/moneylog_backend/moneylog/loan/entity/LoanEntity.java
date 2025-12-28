package com.moneylog_backend.moneylog.loan.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.loan.dto.LoanDto;
import com.moneylog_backend.global.common.entity.BankEntity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class LoanEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int loan_id;
    @Column(columnDefinition = "")
    private int amount;
    @Column(columnDefinition = "")
    private double interest_rate;
    @Column(columnDefinition = "")
    private LocalDate terminated_at;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BankEntity bankEntity;

    public LoanDto toDto(){
        return LoanDto.builder()
                .loan_id(this.loan_id)
                .bank_id(this.bankEntity != null ? this.bankEntity.getBank_id() : 0)
                .amount(this.amount)
                .interest_rate(this.interest_rate)
                .terminated_at(this.terminated_at)
                .created_at(this.getCreated_at())
                .updated_at(this.getUpdated_at())
                .build();
    } // func end
} // class end