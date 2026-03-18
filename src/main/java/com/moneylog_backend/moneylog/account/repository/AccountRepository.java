package com.moneylog_backend.moneylog.account.repository;

import com.moneylog_backend.moneylog.account.entity.AccountEntity;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    List<AccountEntity> findByUserId (Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.accountId = :accountId")
    Optional<AccountEntity> findByIdForUpdate (@Param("accountId") Integer accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.accountId IN :accountIds ORDER BY a.accountId ASC")
    List<AccountEntity> findAllByAccountIdInOrderByAccountIdAscForUpdate (@Param("accountIds") List<Integer> accountIds);
}
