package com.moneylog_backend.moneylog.ledger.entity;

import com.moneylog_backend.global.common.BaseTime;
import com.moneylog_backend.moneylog.ledger.dto.TransferDto;

import org.hibernate.annotations.DynamicInsert;

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
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
public class TransferEntity extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id", columnDefinition = "INT UNSIGNED")
    private Integer transferId;
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer userId;
    @Column(name = "from_account", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer fromAccount;
    @Column(name = "to_account", columnDefinition = "INT UNSIGNED NOT NULL")
    private Integer toAccount;
    @Column(columnDefinition = "INT NOT NULL")
    private Integer amount;
    @Column(name = "transfer_at", columnDefinition = "DATE NOT NULL")
    private LocalDate transferAt;
    @Column(columnDefinition = "TEXT")
    private String memo;

    public TransferDto toDto () {
        return TransferDto.builder()
                          .transferId(this.transferId)
                          .userId(this.userId)
                          .fromAccount(this.fromAccount)
                          .toAccount(this.toAccount)
                          .amount(this.amount)
                          .transferAt(this.transferAt)
                          .memo(this.memo)
                          .createdAt(this.getCreatedAt())
                          .updatedAt(this.getUpdatedAt())
                          .build();
    }
}