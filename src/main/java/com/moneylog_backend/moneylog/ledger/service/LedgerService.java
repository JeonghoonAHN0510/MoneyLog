package com.moneylog_backend.moneylog.ledger.service;

import java.time.LocalDate;
import java.util.List;

import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.ledger.dto.LedgerDto;
import com.moneylog_backend.moneylog.ledger.dto.query.SelectLedgerByUserIdQuery;
import com.moneylog_backend.moneylog.ledger.entity.LedgerEntity;
import com.moneylog_backend.moneylog.ledger.mapper.LedgerMapper;
import com.moneylog_backend.moneylog.ledger.repository.LedgerRepository;
import com.moneylog_backend.moneylog.payment.repository.PaymentRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LedgerService {
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final CategoryMapper categoryMapper;
    private final LedgerMapper ledgerMapper;

    @Transactional
    public int saveLedger (LedgerDto ledgerDto) {
        AccountEntity accountEntity = getAccountByLedgerDto(ledgerDto);

        String type = categoryMapper.getCategoryTypeByCategoryId(ledgerDto.getCategory_id());
        if (type == null) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        if ("EXPENSE".equals(type)) {
            if (!paymentRepository.existsById(ledgerDto.getPayment_id())) {
                throw new IllegalArgumentException("유효하지 않은 결제 수단입니다.");
            }
            accountEntity.withdraw(ledgerDto.getAmount());
        } else if ("INCOME".equals(type)) {
            accountEntity.deposit(ledgerDto.getAmount());
        }

        LedgerEntity ledgerEntity = ledgerDto.toEntity();
        ledgerRepository.save(ledgerEntity);

        return ledgerEntity.getLedger_id();
    }

    public List<LedgerDto> getLedgersByUserId (int user_id) {
        LocalDate end_date = LocalDate.now();
        LocalDate start_date = end_date.withDayOfMonth(1);

        SelectLedgerByUserIdQuery selectQuery = SelectLedgerByUserIdQuery.builder()
                                                                         .user_id(user_id)
                                                                         .start_date(start_date)
                                                                         .end_date(end_date)
                                                                         .build();
        return ledgerMapper.getLedgersByUserId(selectQuery);
    }

    @Transactional
    public LedgerDto updateLedger (LedgerDto ledgerDto) {
        LedgerEntity ledgerEntity = getLedgerByLedgerDto(ledgerDto);
        AccountEntity previousAccountEntity = getAccountByLedgerDto(ledgerEntity.toDto());
        AccountEntity accountEntity = getAccountByLedgerDto(ledgerDto);

        String previousType = categoryMapper.getCategoryTypeByCategoryId(ledgerEntity.getCategory_id());
        int previousBalance = ledgerEntity.getAmount();

        if (previousType.equals("EXPENSE")) {
            previousAccountEntity.deposit(previousBalance);
        } else if (previousType.equals("INCOME")) {
            previousAccountEntity.withdraw(previousBalance);
        }

        int category_id = ledgerDto.getCategory_id();
        String InputType = categoryMapper.getCategoryTypeByCategoryId(category_id);
        int InputBalance = ledgerDto.getAmount();

        if (InputType == null) {
            throw new IllegalArgumentException("유효하지 않은 카테고리입니다.");
        }

        if (InputType.equals("EXPENSE")) {
            accountEntity.withdraw(InputBalance);
        } else if (InputType.equals("INCOME")) {
            accountEntity.deposit(InputBalance);
        }

        ledgerEntity.setCategory_id(category_id);
        ledgerEntity.setAmount(InputBalance);

        Integer paymentId = ledgerDto.getPayment_id();

        if (paymentId != null) {
            if (!paymentRepository.existsById(paymentId)) {
                throw new IllegalArgumentException("유효하지 않은 결제 수단입니다.");
            }
            ledgerEntity.setPayment_id(paymentId);
        }

        String InputTitle = ledgerDto.getTitle();
        if (InputTitle != null) {
            ledgerEntity.setTitle(InputTitle);
        }

        String InputMemo = ledgerDto.getMemo();
        if (InputMemo != null) {
            ledgerEntity.setMemo(InputMemo);
        }

        LocalDate InputTradingAt = ledgerDto.getTrading_at();
        if (InputTradingAt != null) {
            ledgerEntity.setTrading_at(InputTradingAt);
        }

        ledgerEntity.setAccount_id(ledgerDto.getAccount_id());

        return ledgerEntity.toDto();
    }

    @Transactional
    public boolean deleteLedger (LedgerDto ledgerDto) {
        LedgerEntity ledgerEntity = getLedgerByLedgerDto(ledgerDto);

        ledgerRepository.delete(ledgerEntity);
        return true;
    }

    private LedgerEntity getLedgerByLedgerDto (LedgerDto ledgerDto) {
        LedgerEntity ledgerEntity = ledgerRepository.findById(ledgerDto.getLedger_id())
                                                    .orElseThrow(
                                                        () -> new IllegalArgumentException("존재하지 않는 지출 내역입니다."));
        if (!ledgerDto.getUser_id().equals(ledgerEntity.getUser_id())) {
            throw new AccessDeniedException("본인의 지출 내역이 아닙니다.");
        }

        return ledgerEntity;
    }

    private AccountEntity getAccountByLedgerDto (LedgerDto ledgerDto) {
        AccountEntity accountEntity = accountRepository.findById(ledgerDto.getAccount_id())
                                                       .orElseThrow(
                                                           () -> new IllegalArgumentException("존재하지 않는 계좌입니다."));
        if (!accountEntity.getUser_id().equals(ledgerDto.getUser_id())) {
            throw new AccessDeniedException("본인의 계좌가 아닙니다.");
        }

        return accountEntity;
    }
}