package com.moneylog_backend.moneylog.account.repository;

import com.moneylog_backend.moneylog.account.entity.AccountEntity;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<AccountEntity, Integer> {
}