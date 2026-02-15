package com.moneylog_backend.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.error.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. @Valid 유효성 검사 실패 (@NotNull, @Range, @NotBlank 등 모두 여기서 처리)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                                .collect(java.util.stream.Collectors.joining(", "));
        ErrorResponse response = new ErrorResponse("INVALID_INPUT", errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. JSON 파싱 에러 (예: Integer 필드에 "abc" 문자열을 보낸 경우)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonErrors(HttpMessageNotReadableException ex) {
        ErrorResponse response = new ErrorResponse("INVALID_JSON", ErrorMessageConstants.INVALID_JSON);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 3. 권한 없음 처리 (AccessDeniedException) -> 403 Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = new ErrorResponse("ACCESS_DENIED", ErrorMessageConstants.ACCESS_DENIED);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 4. 잘못된 인자 처리 (IllegalArgumentException) -> 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse response = new ErrorResponse("INVALID_INPUT", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 4-1.비밀번호 불일치 (BadCredentialsException) -> 401 Unauthorized
    @ExceptionHandler({org.springframework.security.authentication.BadCredentialsException.class, org.springframework.security.authentication.InternalAuthenticationServiceException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex) {
        ErrorResponse response = new ErrorResponse("LOGIN_FAILED", ErrorMessageConstants.LOGIN_FAILED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 5. 리소스 없음 처리 -> 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse response = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 6. ResponseStatusException 처리 -> 상태코드/메시지 표준화
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        String errorCode = status != null ? status.name() : "HTTP_" + ex.getStatusCode().value();

        String reason = ex.getReason();
        String errorMessage = (reason != null && !reason.isBlank())
            ? reason
            : getDefaultMessageByStatus(status);

        ErrorResponse response = new ErrorResponse(errorCode, errorMessage);
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    private String getDefaultMessageByStatus(HttpStatus status) {
        if (status == null) {
            return ErrorMessageConstants.INTERNAL_SERVER_ERROR;
        }
        return switch (status) {
            case BAD_REQUEST -> ErrorMessageConstants.BAD_REQUEST;
            case UNAUTHORIZED -> ErrorMessageConstants.LOGIN_FAILED;
            case FORBIDDEN -> ErrorMessageConstants.ACCESS_DENIED;
            case NOT_FOUND -> ErrorMessageConstants.NOT_FOUND;
            case CONFLICT -> ErrorMessageConstants.CONFLICT;
            default -> ErrorMessageConstants.INTERNAL_SERVER_ERROR;
        };
    }

    // 7. 그 외 예상치 못한 모든 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("Internal server error", ex);
        ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", ErrorMessageConstants.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
