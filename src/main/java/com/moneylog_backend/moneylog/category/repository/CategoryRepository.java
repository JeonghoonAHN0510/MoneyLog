package com.moneylog_backend.moneylog.category.repository;

import com.moneylog_backend.moneylog.category.entity.CategoryEntity;

import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<CategoryEntity, Integer> {
} // interface end