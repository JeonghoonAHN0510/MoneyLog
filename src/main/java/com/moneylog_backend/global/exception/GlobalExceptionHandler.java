package com.moneylog_backend.global.exception;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.moneylog_backend.global.constant.ErrorMessageConstants;
import com.moneylog_backend.global.error.ErrorResponse;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Set<String> KNOWN_CONSTRAINT_CODE_PREFIXES = Set.of(
        "NotBlank", "NotNull", "Email", "Pattern", "Size", "Min", "Max", "Range"
    );
    private static final Map<String, Class<? extends Annotation>> CONSTRAINT_ANNOTATIONS = Map.of(
        "NotBlank", NotBlank.class,
        "NotNull", NotNull.class,
        "Email", Email.class,
        "Pattern", Pattern.class,
        "Size", Size.class,
        "Min", Min.class,
        "Max", Max.class,
        "Range", Range.class
    );
    private static final Map<String, String> FIELD_SPECIFIC_MESSAGES = Map.of(
        "balance", "잔액은 0원 이상이어야 합니다.",
        "amount", "금액은 1원 이상이어야 합니다.",
        "dayOfWeek", "요일은 1~7 사이여야 합니다.",
        "dayOfMonth", "실행일은 1~31 사이여야 합니다."
    );

    // 1. @Valid 유효성 검사 실패 (@NotNull, @Range, @NotBlank 등 모두 여기서 처리)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Object target = ex.getBindingResult().getTarget();
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> resolveValidationMessage(fieldError, target))
                                .distinct()
                                .collect(Collectors.joining(", "));
        ErrorResponse response = new ErrorResponse("INVALID_INPUT", errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private String resolveValidationMessage(FieldError fieldError, Object target) {
        String defaultMessage = fieldError.getDefaultMessage();
        if (hasExplicitCustomMessage(fieldError, target, defaultMessage)) {
            return defaultMessage;
        }

        String field = fieldError.getField();
        String specificMessage = FIELD_SPECIFIC_MESSAGES.get(field);
        if (specificMessage != null) {
            return specificMessage;
        }

        if (isKnownConstraintCode(fieldError)) {
            return getFieldSpecificValidationMessage(fieldError);
        }

        if (defaultMessage != null && !defaultMessage.isBlank()) {
            return defaultMessage;
        }
        return field + " 값이 올바르지 않습니다.";
    }

    private boolean hasExplicitCustomMessage(FieldError fieldError, Object target, String defaultMessage) {
        if (defaultMessage == null || defaultMessage.isBlank() || target == null) {
            return false;
        }

        String constraintCode = extractConstraintCode(fieldError.getCodes());
        if (constraintCode == null) {
            return false;
        }

        Class<? extends Annotation> annotationClass = CONSTRAINT_ANNOTATIONS.get(constraintCode);
        if (annotationClass == null) {
            return false;
        }

        Field field = findField(target.getClass(), fieldError.getField());
        if (field == null) {
            return false;
        }

        Annotation annotation = field.getAnnotation(annotationClass);
        if (annotation == null) {
            return false;
        }

        String configuredMessage = readAnnotationMessage(annotation);
        String defaultTemplate = readDefaultMessageTemplate(annotationClass);

        return configuredMessage != null
            && defaultTemplate != null
            && !configuredMessage.equals(defaultTemplate);
    }

    private Field findField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private String readAnnotationMessage(Annotation annotation) {
        try {
            Object value = annotation.annotationType().getMethod("message").invoke(annotation);
            return value instanceof String ? (String) value : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private String readDefaultMessageTemplate(Class<? extends Annotation> annotationClass) {
        try {
            Object defaultValue = annotationClass.getMethod("message").getDefaultValue();
            return defaultValue instanceof String ? (String) defaultValue : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private String extractConstraintCode(String[] codes) {
        if (codes == null) {
            return null;
        }
        for (String code : codes) {
            if (code == null) {
                continue;
            }
            for (String prefix : KNOWN_CONSTRAINT_CODE_PREFIXES) {
                if (code.startsWith(prefix)) {
                    return prefix;
                }
            }
        }
        return null;
    }

    private boolean isKnownConstraintCode(FieldError fieldError) {
        return extractConstraintCode(fieldError.getCodes()) != null;
    }

    private String getFieldSpecificValidationMessage(FieldError fieldError) {
        String field = fieldError.getField();
        Object[] arguments = fieldError.getArguments();
        String constraintCode = extractConstraintCode(fieldError.getCodes());

        if (constraintCode == null) {
            return field + " 값이 올바르지 않습니다.";
        }

        return switch (constraintCode) {
            case "NotBlank", "NotNull" -> field + " 항목은 필수입니다.";
            case "Email", "Pattern" -> field + " 형식이 올바르지 않습니다.";
            case "Size" -> {
                Number min = getNumberArgument(arguments, 2);
                Number max = getNumberArgument(arguments, 1);
                if (min != null && max != null) {
                    yield String.format("%s 길이는 %d에서 %d 사이여야 합니다.", field, min.longValue(), max.longValue());
                }
                yield field + " 길이가 허용 범위를 벗어났습니다.";
            }
            case "Min" -> {
                Number min = getNumberArgument(arguments, 1);
                if (min != null) {
                    yield String.format("%s 값은 %d 이상이어야 합니다.", field, min.longValue());
                }
                yield field + " 값이 허용된 최소값보다 작습니다.";
            }
            case "Max" -> {
                Number max = getNumberArgument(arguments, 1);
                if (max != null) {
                    yield String.format("%s 값은 %d 이하의 값이어야 합니다.", field, max.longValue());
                }
                yield field + " 값이 허용된 최대값보다 큽니다.";
            }
            case "Range" -> {
                Number min = getNumberArgument(arguments, 2);
                Number max = getNumberArgument(arguments, 1);
                if (min != null && max != null) {
                    yield String.format("%s 값은 %d에서 %d 사이여야 합니다.", field, min.longValue(), max.longValue());
                }
                yield field + " 값이 허용된 범위를 벗어났습니다.";
            }
            default -> field + " 값이 올바르지 않습니다.";
        };
    }

    private Number getNumberArgument(Object[] arguments, int index) {
        if (arguments == null || arguments.length <= index) {
            return null;
        }
        Object argument = arguments[index];
        if (argument instanceof Number number) {
            return number;
        }
        return null;
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
    @ExceptionHandler({ BadCredentialsException.class, InternalAuthenticationServiceException.class})
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
