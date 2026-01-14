package com.moneylog_backend.moneylog.account.repository;

import com.moneylog_backend.moneylog.account.entity.AccountEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {}