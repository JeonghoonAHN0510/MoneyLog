package com.moneylog_backend.moneylog.fixed.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.util.OwnershipValidator;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.fixed.dto.req.FixedReqDto;
import com.moneylog_backend.moneylog.fixed.dto.res.FixedResDto;
import com.moneylog_backend.moneylog.fixed.entity.FixedEntity;
import com.moneylog_backend.moneylog.fixed.mapper.FixedMapper;
import com.moneylog_backend.moneylog.fixed.repository.FixedRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FixedService {
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final FixedRepository fixedRepository;
    private final FixedMapper fixedMapper;

    @Transactional
    public FixedResDto saveFixed (FixedReqDto fixedReqDto, Integer userId) {
        validateOwnership(fixedReqDto.getAccountId(), fixedReqDto.getCategoryId(), userId);

        LocalDate endDate = fixedReqDto.getEndDate();
        if (endDate != null && fixedReqDto.getStartDate().isAfter(endDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        FixedEntity fixedEntity = fixedReqDto.toEntity(userId);
        fixedRepository.save(fixedEntity);

        return fixedEntity.toResDto();
    }

    private void validateOwnership (Integer accountId, Integer categoryId, Integer userId) {
        AccountEntity account = accountRepository.findById(accountId)
                                                 .orElseThrow(() -> new ResourceNotFoundException(
                                                     ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        OwnershipValidator.validateOwner(account.getUserId(), userId, "본인의 계좌가 아닙니다.");

        CategoryEntity category = categoryRepository.findById(categoryId)
                                                    .orElseThrow(
                                                        () -> new ResourceNotFoundException(
                                                            ErrorMessageConstants.CATEGORY_NOT_FOUND));

        OwnershipValidator.validateOwner(category.getUserId(), userId, "본인의 카테고리가 아닙니다.");
    }
}
