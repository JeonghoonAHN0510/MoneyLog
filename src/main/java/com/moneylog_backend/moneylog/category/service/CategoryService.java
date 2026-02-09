package com.moneylog_backend.moneylog.category.service;

import java.util.List;

import com.moneylog_backend.global.exception.ResourceNotFoundException;
import com.moneylog_backend.moneylog.category.dto.req.CategoryReqDto;
import com.moneylog_backend.moneylog.category.dto.res.CategoryResDto;
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
    public int saveCategory(CategoryReqDto categoryReqDto, int userId) {
        int countSameCategory = categoryMapper.checkCategoryNameTypeUnique(categoryReqDto);
        if (countSameCategory > 0) {
            return -1;
        }

        CategoryEntity categoryEntity = categoryReqDto.toEntity(userId);
        categoryEntity = categoryRepository.save(categoryEntity);

        return categoryEntity.getCategoryId();
    }

    public List<CategoryResDto> getCategoryByUserId(int userId) {

        List<CategoryEntity> categoryEntities = categoryRepository.findByUserId(userId);

        return categoryEntities.stream().map(CategoryEntity::toResDto).toList();
    }

    @Transactional
    public CategoryResDto updateCategory(CategoryReqDto categoryReqDto, int userId) {
        CategoryEntity categoryEntity = getCategoryByIdAndValidateOwnership(categoryReqDto.getCategoryId(), userId);

        categoryEntity.updateDetails(categoryReqDto.getName(), categoryReqDto.getType(), categoryReqDto.getColor());

        return categoryEntity.toResDto();
    }

    @Transactional
    public boolean deleteCategory (int categoryId, int userId) {
        CategoryEntity categoryEntity = getCategoryByIdAndValidateOwnership(categoryId, userId);

        categoryRepository.delete(categoryEntity);
        return true;
    }

    private CategoryEntity getCategoryByIdAndValidateOwnership (int categoryId, int userId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                                                          .orElseThrow(
                                                              () -> new ResourceNotFoundException("존재하지 않는 카테고리입니다."));

        if (categoryEntity.getUserId() != userId) {
            throw new AccessDeniedException("본인의 카테고리가 아닙니다.");
        }

        return categoryEntity;
    }
}