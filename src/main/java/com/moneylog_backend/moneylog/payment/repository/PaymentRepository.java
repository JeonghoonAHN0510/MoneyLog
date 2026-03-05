package com.moneylog_backend.moneylog.payment.repository;

import com.moneylog_backend.moneylog.payment.entity.PaymentEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {
    List<PaymentEntity> findByUserId (Integer userId);
}
