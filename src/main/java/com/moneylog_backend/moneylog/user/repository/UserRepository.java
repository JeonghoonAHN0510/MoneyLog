package com.moneylog_backend.moneylog.user.repository;

import java.util.Optional;

import com.moneylog_backend.moneylog.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByLoginId (String loginId);

    boolean existsByLoginId (String id);

    boolean existsByEmailHash(String emailHash);

    @Query(
        value = "select count(*) from user where email_hash is null and lower(email) = lower(:email)",
        nativeQuery = true
    )
    long countLegacyPlainEmail(@Param("email") String email);
}
