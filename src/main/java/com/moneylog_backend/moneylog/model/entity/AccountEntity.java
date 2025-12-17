package com.moneylog_backend.moneylog.model.entity;

import com.moneylog_backend.moneylog.model.dto.AccountDto;

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
public class AccountEntity extends BaseTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int account_id;
    @Column(columnDefinition = "VARCHAR(50)")
    private String nickname;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String bank_name;
    @Column(columnDefinition = "INT")
    private int balance;
    @Column(columnDefinition = "VARCHAR(50) NOT NULL")
    private String account_number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    public AccountDto toDto(){
        return AccountDto.builder()
                .account_id(this.account_id)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .nickname(this.nickname)
                .bank_name(this.bank_name)
                .balance(this.balance)
                .account_number(this.account_number)
                .build();
    } // func end
} // class end