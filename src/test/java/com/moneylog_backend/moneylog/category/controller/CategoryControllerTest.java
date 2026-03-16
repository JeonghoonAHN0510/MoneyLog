package com.moneylog_backend.moneylog.category.controller;

import com.moneylog_backend.global.type.CategoryEnum;
import com.moneylog_backend.moneylog.category.dto.req.CategoryReqDto;
import com.moneylog_backend.moneylog.category.service.CategoryService;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryControllerTest {

    @Test
    void 카테고리_저장시_중복이면_400을_반환한다() {
        CategoryService categoryService = mock(CategoryService.class);
        CategoryController controller = new CategoryController(categoryService);
        CategoryReqDto requestDto = CategoryReqDto.builder()
                                                  .name("식비")
                                                  .type(CategoryEnum.EXPENSE)
                                                  .build();
        when(categoryService.saveCategory(requestDto, 1)).thenReturn(-1);

        ResponseEntity<?> response = controller.saveCategory(requestDto, 1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(categoryService).saveCategory(requestDto, 1);
    }

    @Test
    void 카테고리_저장시_성공하면_식별자를_반환한다() {
        CategoryService categoryService = mock(CategoryService.class);
        CategoryController controller = new CategoryController(categoryService);
        CategoryReqDto requestDto = CategoryReqDto.builder()
                                                  .name("급여")
                                                  .type(CategoryEnum.INCOME)
                                                  .build();
        when(categoryService.saveCategory(requestDto, 1)).thenReturn(10001);

        ResponseEntity<?> response = controller.saveCategory(requestDto, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10001, response.getBody());
        verify(categoryService).saveCategory(requestDto, 1);
    }
}
