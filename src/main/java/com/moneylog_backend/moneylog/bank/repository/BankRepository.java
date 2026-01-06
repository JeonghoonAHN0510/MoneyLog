package com.moneylog_backend.moneylog.bank.repository;

import org.springframework.data.repository.CrudRepository;

import com.moneylog_backend.moneylog.bank.entity.BankEntity;

public interface BankRepository extends CrudRepository<BankEntity, Integer> {

}
