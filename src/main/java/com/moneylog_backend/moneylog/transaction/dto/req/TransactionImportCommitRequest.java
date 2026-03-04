package com.moneylog_backend.moneylog.transaction.dto.req;

import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionImportCommitRequest {
    @Size(min = 1, message = "저장할 거래가 없습니다.")
    private List<TransactionImportCommitRowDto> rows;
}
