package com.moneylog_backend.moneylog.fixed.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneylog_backend.moneylog.fixed.entity.FixedEntity;

public interface FixedRepository extends JpaRepository<FixedEntity, Integer> {}
