package com.moneylog_backend.moneylog.user.repository;

import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {

    UserEntity findById(String id);

    boolean existsByEmail(String email);
} // interface end