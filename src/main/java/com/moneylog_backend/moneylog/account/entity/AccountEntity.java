package com.moneylog_backend.moneylog.account.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.moneylog.account.dto.AccountDto;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account")
@Data
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", columnDefinition = "INT UNSIGNED")
    private Integer accountId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "bank_id", columnDefinition = "INT UNSIGNED")
    private Integer bankId;
    @Column(columnDefinition = "VARCHAR(50)")
    private String nickname;
    @Column(columnDefinition = "INT")
    private Integer balance;
    @Column(name = "account_number", columnDefinition = "VARCHAR(50)")
    private String accountNumber;
    @Column(columnDefinition = "ENUM('RED', 'AMBER', 'YELLOW', 'LIME', 'GREEN', 'EMERALD', 'TEAL', 'CYAN', 'BLUE', 'PURPLE', 'PINK', 'SLATE') DEFAULT 'BLUE'")
    @Enumerated(EnumType.STRING)
    private ColorEnum color;
    @Column(columnDefinition = "ENUM('BANK', 'CASH', 'POINT', 'OTHER') NOT NULL")
    @Enumerated(EnumType.STRING)
    private AccountTypeEnum type;

    public void deposit(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입금액은 0원보다 커야 합니다.");
        }
        this.balance += amount;
    }

    public void withdraw(Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("출금액은 0원보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("잔액이 부족합니다. (현재 잔액: " + this.balance + ")");
        }
        this.balance -= amount;
    }

    public AccountDto toDto () {
        return AccountDto.builder()
                         .accountId(this.accountId)
                         .userId(this.userId)
                         .bankId(this.bankId)
                         .nickname(this.nickname)
                         .balance(this.balance)
                         .accountNumber(this.accountNumber)
                         .color(this.color)
                         .type(this.type)
                         .createdAt(this.getCreatedAt())
                         .updatedAt(this.getUpdatedAt())
                         .build();
    }
}