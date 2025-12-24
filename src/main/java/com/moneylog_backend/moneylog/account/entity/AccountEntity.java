package com.moneylog_backend.moneylog.account.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.account.dto.AccountDto;
import com.moneylog_backend.global.common.entity.BankEntity;
import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int account_id;
    @Column(columnDefinition = "VARCHAR(50)")
    private String nickname;
    @Column(columnDefinition = "INT")
    private int balance;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String account_number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BankEntity bankEntity;

    public AccountDto toDto(){
        return AccountDto.builder()
                .account_id(this.account_id)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .bank_id(this.bankEntity != null ? this.bankEntity.getBank_id() : 0)
                .nickname(this.nickname)
                .balance(this.balance)
                .account_number(this.account_number)
                .build();
    } // func end
} // class end