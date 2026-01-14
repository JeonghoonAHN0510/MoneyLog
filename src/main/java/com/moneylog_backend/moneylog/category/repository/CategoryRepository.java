package com.moneylog_backend.moneylog.category.repository;

import java.util.List;

import com.moneylog_backend.moneylog.category.entity.CategoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {

    @Query("SELECT c FROM CategoryEntity c WHERE c.user_id = :user_id")
    List<CategoryEntity> findByUser_id (int user_id);
}