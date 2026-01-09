package com.moneylog_backend.moneylog.payment.repository;

import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {
} // interface end