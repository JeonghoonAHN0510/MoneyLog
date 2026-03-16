package com.moneylog_backend.moneylog.budget.controller;

import com.moneylog_backend.moneylog.budget.dto.req.BudgetReqDto;
import com.moneylog_backend.moneylog.budget.dto.res.BudgetResDto;
import com.moneylog_backend.moneylog.budget.service.BudgetService;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BudgetControllerTest {

    @Test
    void 예산_저장시_중복이면_400을_반환한다() {
        BudgetService budgetService = mock(BudgetService.class);
        BudgetController controller = new BudgetController(budgetService);
        BudgetReqDto requestDto = BudgetReqDto.builder()
                                              .categoryId(10001)
                                              .amount(300000)
                                              .build();
        when(budgetService.saveBudget(requestDto, 1)).thenReturn(-1);

        ResponseEntity<?> response = controller.saveBudget(requestDto, 1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(budgetService).saveBudget(requestDto, 1);
    }

    @Test
    void 예산_수정시_null이면_400을_반환한다() {
        BudgetService budgetService = mock(BudgetService.class);
        BudgetController controller = new BudgetController(budgetService);
        BudgetReqDto requestDto = BudgetReqDto.builder()
                                              .budgetId(40001)
                                              .categoryId(10001)
                                              .amount(500000)
                                              .build();
        when(budgetService.updateBudget(requestDto, 1)).thenReturn(null);

        ResponseEntity<?> response = controller.updateBudget(requestDto, 1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(budgetService).updateBudget(requestDto, 1);
    }

    @Test
    void 예산_수정시_성공하면_응답본문을_반환한다() {
        BudgetService budgetService = mock(BudgetService.class);
        BudgetController controller = new BudgetController(budgetService);
        BudgetReqDto requestDto = BudgetReqDto.builder()
                                              .budgetId(40001)
                                              .categoryId(10001)
                                              .amount(500000)
                                              .build();
        BudgetResDto responseDto = BudgetResDto.builder()
                                               .budgetId(40001)
                                               .userId(1)
                                               .categoryId(10001)
                                               .amount(500000)
                                               .build();
        when(budgetService.updateBudget(requestDto, 1)).thenReturn(responseDto);

        ResponseEntity<?> response = controller.updateBudget(requestDto, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(budgetService).updateBudget(requestDto, 1);
    }
}
