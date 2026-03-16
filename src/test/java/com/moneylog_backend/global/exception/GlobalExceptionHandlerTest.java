package com.moneylog_backend.global.exception;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.error.ErrorResponse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void conflict_ResponseStatusException은_표준_에러_envelope로_변환된다() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.CONFLICT, ErrorMessageConstants.CONFLICT);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResponseStatusException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().getErrorCode());
        assertEquals(ErrorMessageConstants.CONFLICT, response.getBody().getErrorMessage());
    }

    @Test
    void conflict_ResponseStatusException에_reason이_없으면_기본_충돌_메시지를_사용한다() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.CONFLICT);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResponseStatusException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().getErrorCode());
        assertEquals(ErrorMessageConstants.CONFLICT, response.getBody().getErrorMessage());
    }
}
