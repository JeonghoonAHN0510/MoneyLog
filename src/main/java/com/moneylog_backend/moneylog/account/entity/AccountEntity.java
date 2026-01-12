package com.moneylog_backend.moneylog.account.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.global.type.AccountColorEnum;
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
    private int account_id;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int user_id;
    @Column(name = "bank_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private int bank_id;
    @Column(columnDefinition = "VARCHAR(50)")
    private String nickname;
    @Column(columnDefinition = "INT")
    private int balance;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String account_number;
    @Column(columnDefinition = "ENUM('BLUE', 'RED', 'GREEN', 'YELLOW', 'PURPLE', 'PINK', 'CYAN') NOT NULL")
    @Enumerated(EnumType.STRING)
    private AccountColorEnum color;
    @Column(columnDefinition = "ENUM('BANK', 'CASH', 'POINT', 'OTHER') NOT NULL")
    @Enumerated(EnumType.STRING)
    private AccountTypeEnum type;

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