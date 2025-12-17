package com.moneylog_backend.moneylog.model.repository;

import com.moneylog_backend.moneylog.model.entity.UserEntity;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {

} // interface end