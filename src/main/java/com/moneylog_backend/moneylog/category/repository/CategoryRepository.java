package com.moneylog_backend.moneylog.category.repository;

import com.moneylog_backend.moneylog.category.entity.CategoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Integer> {
} // interface end