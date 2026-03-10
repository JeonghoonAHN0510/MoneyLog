package com.moneylog_backend.moneylog.user.service;

import com.moneylog_backend.global.security.pii.PiiCryptoService;
import com.moneylog_backend.moneylog.user.entity.UserEntity;
import com.moneylog_backend.moneylog.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserEmailHashBackfillService {
    private final UserRepository userRepository;
    private final PiiCryptoService piiCryptoService;

    @Transactional
    public int backfillMissingEmailHashes() {
        List<UserEntity> usersWithoutEmailHash = userRepository.findAllByEmailHashIsNullOrderByUserIdAsc();
        if (usersWithoutEmailHash.isEmpty()) {
            return 0;
        }

        Map<String, Integer> seenHashes = new HashMap<>();
        for (UserEntity userEntity : usersWithoutEmailHash) {
            String normalizedEmail = piiCryptoService.normalizeEmail(userEntity.getEmail());
            String emailHash = piiCryptoService.hashEmail(normalizedEmail);

            Integer duplicatedUserId = seenHashes.putIfAbsent(emailHash, userEntity.getUserId());
            if (duplicatedUserId != null) {
                throw new IllegalStateException(
                    "email_hash backfill 중 중복 이메일이 감지되었습니다. userId=" + duplicatedUserId + "," + userEntity.getUserId()
                );
            }

            if (userRepository.existsByEmailHash(emailHash)) {
                throw new IllegalStateException(
                    "email_hash backfill 중 기존 해시와 충돌이 감지되었습니다. userId=" + userEntity.getUserId()
                );
            }

            userEntity.updateEmailHash(emailHash);
        }

        return usersWithoutEmailHash.size();
    }
}
