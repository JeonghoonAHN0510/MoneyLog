package com.moneylog_backend.moneylog.category.service;

import java.util.List;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.global.type.ColorEnum;
import com.moneylog_backend.moneylog.category.dto.CategoryDto;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;

import org.springframework.security.access.AccessDeniedException;
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
    public int saveCategory (CategoryDto categoryDto, int userId) {
        int countSameCategory = categoryMapper.checkCategoryNameTypeUnique(categoryDto);
        if (countSameCategory > 0) {
            return -1;
        }

        CategoryEntity categoryEntity = categoryDto.toEntity(userId);
        categoryEntity = categoryRepository.save(categoryEntity);

        return categoryEntity.getCategoryId();
    }

    public List<CategoryDto> getCategoryByUserId (int userId) {

        List<CategoryEntity> categoryEntities = categoryRepository.findByUserId(userId);

        return categoryEntities.stream().map(CategoryEntity::toDto).toList();
    }

    @Transactional
    public CategoryDto updateCategory (CategoryDto categoryDto, int userId) {
        CategoryEntity categoryEntity = getCategoryEntityById(categoryDto.getCategoryId(), userId);

        categoryEntity.updateDetails(
            categoryDto.getName(),
            categoryDto.getType(),
            categoryDto.getColor()
        );

        return categoryEntity.toDto();
    }

    @Transactional
    public boolean deleteCategory (int categoryId, int userId) {
        CategoryEntity categoryEntity = getCategoryEntityById(categoryId, userId);

        categoryRepository.delete(categoryEntity);
        return true;
    }

    private CategoryEntity getCategoryEntityById (int categoryId, int userId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                                                          .orElseThrow(
                                                              () -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        if (categoryEntity.getUserId() != userId) {
            throw new AccessDeniedException("본인의 카테고리가 아닙니다.");
        }

        return categoryEntity;
    }
}