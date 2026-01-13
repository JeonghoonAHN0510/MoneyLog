package com.moneylog_backend.moneylog.category.service;

import com.moneylog_backend.moneylog.category.dto.CategoryDto;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;
import com.moneylog_backend.moneylog.category.mapper.CategoryMapper;
import com.moneylog_backend.moneylog.category.repository.CategoryRepository;
import com.moneylog_backend.moneylog.user.service.UserService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    public int saveCategory (CategoryDto categoryDto, String login_id) {
        int user_pk = userService.getUserPK(login_id);
        categoryDto.setUser_id(user_pk);

        int countSameCategory = categoryMapper.checkCategoryNameTypeUnique(categoryDto);
        if (countSameCategory > 0) {
            return -1;
        }

        CategoryEntity categoryEntity = categoryDto.toEntity();
        categoryEntity = categoryRepository.save(categoryEntity);

        return categoryEntity.getCategory_id();
    }
} // class end