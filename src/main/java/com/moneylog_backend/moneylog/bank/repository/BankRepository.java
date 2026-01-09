package com.moneylog_backend.moneylog.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneylog_backend.moneylog.bank.entity.BankEntity;

public interface BankRepository extends JpaRepository<BankEntity, Integer> {

}
