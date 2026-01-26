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
    @Column(columnDefinition = "INT UNSIGNED")
    private Integer account_id;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer user_id;
    @Column(name = "bank_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer bank_id;
    @Column(columnDefinition = "VARCHAR(50)")
    private String nickname;
    @Column(columnDefinition = "INT")
    private Integer balance;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String account_number;
    @Column(columnDefinition = "ENUM('BLUE', 'RED', 'GREEN', 'YELLOW', 'PURPLE', 'PINK', 'CYAN') DEFAULT 'BLUE'")
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
                         .account_id(this.account_id)
                         .user_id(this.user_id)
                         .bank_id(this.bank_id)
                         .nickname(this.nickname)
                         .balance(this.balance)
                         .account_number(this.account_number)
                         .color(this.color)
                         .type(this.type)
                         .created_at(this.getCreated_at())
                         .updated_at(this.getUpdated_at())
                         .build();
    }
}