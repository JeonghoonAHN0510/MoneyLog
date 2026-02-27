package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.global.type.AccountTypeEnum;
import com.moneylog_backend.moneylog.account.entity.AccountEntity;
import com.moneylog_backend.moneylog.account.repository.AccountRepository;
import com.moneylog_backend.moneylog.user.dto.UserDto;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserWriteTxService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public int signup(UserDto userDto,
                      String regexPhone,
                      String profileImageUrl,
                      String encodedPassword,
                      String regexAccountNumber) {
        UserEntity userEntity = userDto.toEntity(regexPhone, profileImageUrl, encodedPassword);
        userRepository.save(userEntity);

        AccountEntity accountEntity = AccountEntity.builder()
                                                   .userId(userEntity.getUserId())
                                                   .bankId(userDto.getBankId())
                                                   .nickname(userDto.getBankName())
                                                   .balance(0)
                                                   .accountNumber(regexAccountNumber)
                                                   .type(AccountTypeEnum.BANK)
                                                   .build();
        accountRepository.save(accountEntity);

        userEntity.setCreatedAccountId(accountEntity.getAccountId());
        return userEntity.getUserId();
    }

    @Transactional
    public ProfileImageUpdateTxResult updateProfileImageUrl(String loginId, String newFileUrl) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                                              .orElseThrow(
                                                  () -> new ResourceNotFoundException(ErrorMessageConstants.USER_NOT_FOUND));

        String oldFileUrl = userEntity.getProfileImageUrl();
        userEntity.updateProfileImageUrl(newFileUrl);

        return new ProfileImageUpdateTxResult(userEntity.excludePassword(), oldFileUrl);
    }
}
