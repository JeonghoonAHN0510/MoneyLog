package com.moneylog_backend.moneylog.model.entity;

import com.moneylog_backend.moneylog.model.dto.TransferDto;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transfer")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferEntity extends BaseTime{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private int transfer_id;
    @Column(columnDefinition = "INT NOT NULL")
    private int amount;
    @Column(columnDefinition = "DATE NOT NULL")
    private LocalDate transfer_at;
    @Column(columnDefinition = "TEXT")
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AccountEntity fromAccountEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account", columnDefinition = "INT UNSIGNED NOT NULL")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AccountEntity toAccountEntity;

    public TransferDto toDto(){
        return TransferDto.builder()
                .transfer_id(this.transfer_id)
                .amount(this.amount)
                .transfer_at(this.transfer_at)
                .memo(this.memo)
                .user_id(this.userEntity != null ? this.userEntity.getUser_id() : 0)
                .from_account(this.fromAccountEntity != null ? this.fromAccountEntity.getAccount_id() : 0)
                .to_account(this.toAccountEntity != null ? this.toAccountEntity.getAccount_id() : 0)
                .created_at(this.getCreated_at())
                .updated_at(this.getUpdated_at())
                .build();
    } // func end
} // class end