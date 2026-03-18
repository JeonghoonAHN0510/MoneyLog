package com.moneylog_backend.global.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.moneylog.category.entity.CategoryEntity;

class GlobalExceptionHandlerLockTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void optimistic_lock_예외는_409_conflict로_변환된다() {
        var response = handler.handleLockConflict(
            new ObjectOptimisticLockingFailureException(CategoryEntity.class, 1)
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals("CONFLICT", response.getBody().getErrorCode());
        assertEquals(ErrorMessageConstants.CONFLICT, response.getBody().getErrorMessage());
    }

    @Test
    void pessimistic_lock_예외는_409_conflict로_변환된다() {
        var response = handler.handleLockConflict(
            new CannotAcquireLockException("lock")
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals("CONFLICT", response.getBody().getErrorCode());
        assertEquals(ErrorMessageConstants.CONFLICT, response.getBody().getErrorMessage());
    }
}
