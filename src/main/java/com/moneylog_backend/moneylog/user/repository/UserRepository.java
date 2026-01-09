package com.moneylog_backend.moneylog.user.repository;

import java.util.Optional;

import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

	Optional<UserEntity> findByLoginId(String loginId);

    boolean existsByLoginId(String id);

    boolean existsByEmail(String email);
} // interface end