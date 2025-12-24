package com.moneylog_backend.moneylog.payment.repository;

import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;

import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<PaymentEntity, Integer> {
} // interface end