package com.moneylog_backend.moneylog.category.service;

import java.util.List;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.category.dto.CategoryDto;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public int saveCategory (CategoryDto categoryDto, int user_id) {
        categoryDto.setUser_id(user_id);

        int countSameCategory = categoryMapper.checkCategoryNameTypeUnique(categoryDto);
        if (countSameCategory > 0) {
            return -1;
        }

        CategoryEntity categoryEntity = categoryDto.toEntity();
        categoryEntity = categoryRepository.save(categoryEntity);

        return categoryEntity.getCategory_id();
    }

    public List<CategoryDto> getCategoryByUserId (int user_id) {

        List<CategoryEntity> categoryEntities = categoryRepository.findByUser_id(user_id);

        return categoryEntities.stream().map(CategoryEntity::toDto).toList();
    }

    @Transactional
    public CategoryDto updateCategory (CategoryDto categoryDto, int user_id) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryDto.getCategory_id()).orElse(null);
        if (categoryEntity == null) {
            return null;
        }

        if (categoryEntity.getUser_id() != user_id) {
            return null;
        }

        String InputName = categoryDto.getName();
        CategoryEnum InputType = categoryDto.getType();
        if (InputName != null) {
            categoryEntity.setName(InputName);
        }
        if (InputType != null) {
            categoryEntity.setType(InputType);
        }

        return categoryEntity.toDto();
    }

    @Transactional
    public boolean deleteCategory (int category_id, int user_id) {
        CategoryEntity categoryEntity = categoryRepository.findById(category_id).orElse(null);
        if (categoryEntity == null) {
            return false;
        }

        if (user_id == categoryEntity.getUser_id()) {
            categoryRepository.deleteById(category_id);
            return true;
        }

        return false;
    }
}