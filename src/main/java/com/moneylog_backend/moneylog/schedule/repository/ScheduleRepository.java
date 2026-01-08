package com.moneylog_backend.moneylog.schedule.repository;

import org.springframework.data.repository.CrudRepository;

import com.moneylog_backend.moneylog.schedule.entity.JobMetaEntity;

public interface ScheduleRepository extends CrudRepository<JobMetaEntity, String> {}
