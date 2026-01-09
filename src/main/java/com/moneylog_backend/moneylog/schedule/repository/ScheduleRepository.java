package com.moneylog_backend.moneylog.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneylog_backend.moneylog.schedule.entity.JobMetaEntity;

public interface ScheduleRepository extends JpaRepository<JobMetaEntity, String> {}
