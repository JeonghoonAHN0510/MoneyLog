package com.moneylog_backend.moneylog.user.repository;

import java.util.Optional;

import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {

	Optional<UserEntity> findByLoginId(String loginId);

    boolean existsByLoginId(String id);

    boolean existsByEmail(String email);
} // interface end